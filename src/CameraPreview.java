package df.lente;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera.Size;
import android.media.AudioManager;
import android.media.MediaScannerConnection;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

/**
 * A specialized camera activity that allows the user to view a preview
 * as well as take photos. This also handles any needed auto focus while the
 * preview is running and before photos are taken.
 * 
 * @author Eric Ostrowski & David Fish
 */

public class CameraPreview extends Activity implements OnTouchListener, OnClickListener, SensorEventListener {

	private static final String TAG = "CameraPreview";
	
	private Camera mCamera = null;
	
	private SensorManager mSensorManager;
	private Sensor mAccelerometer;
	private double mAccX;
	private double mAccY;
	private double mAccZ;
	private final double ACC_THRESH = .40;	//Threshold for motion in G's.
	
	private int mCurrentOrientation;
	private OrientationLstr	mOrientationListener;
	
	private static final int ZOOM_IN = 0;
	private static final int ZOOM_OUT = 1;
	
	private static final int ORI_LAND_UP = 0;
	private static final int ORI_PORT_UP = 1;
	private static final int ORI_LAND_DOWN = 2;
	private static final int ORI_PORT_DOWN = 3;
	
	private int fromDegrees = 90;
	private int toDegrees = 0;
	private int lastOrientation = ORI_LAND_UP;
	
	private RelativeLayout shutterLayout;
	private Animation rotate;
	private Animation anim;

	private RelativeLayout	buttonLayout;
	private ImageButton shutterButton;
	private ImageButton	colorButton;
	private ImageButton	fontButton;
	private ImageButton	importButton;
	
	private int adjustedZoomValue = 0;
	
	final Handler handler = new Handler();

    private float oldX = 0;
    private float oldY = 0;

	private SharedPreferences preferences;
	private static final String NEXT_PHOTO_KEY = "nextPhotoNumber";
	
	private SurfaceView mSurfaceView = null;
	private SurfaceHolder mSurfaceHolder = null;
	
	//Flags
	private boolean mIsPreviewRunning;
	private boolean mIsPictureTaking;
	private boolean mIsFocusStarted;
	private boolean mIsFocusForPicture;
	private boolean mIsFocused;
	private boolean mIsMoving;
	
	private static final String IMAGE_DIR = "/Lente"; //Where to save in device's media directory.
	private static final int SELECT_PICTURE = 1;
	
	//for shutter sound
	private SoundPool soundPool = null;
	private int shutterSound;
	
	/** 
	 * Called when the activity is first created.
	 */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        
        preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		
		//Fullscreen, no title
		getWindow().setFormat(PixelFormat.TRANSLUCENT);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		//Set layout, grab view
		setContentView(R.layout.camera_preview);
        mSurfaceView = (SurfaceView) findViewById(R.id.camera_surface);
        mSurfaceView.setOnTouchListener(this);
	
		//Used to interface with the SurfaceView
		mSurfaceHolder = mSurfaceView.getHolder();
		mSurfaceHolder.addCallback(new SurfaceHolderCb());
		mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        
		//for shutter sound
		soundPool = new SoundPool(1, AudioManager.STREAM_NOTIFICATION, 0);
		shutterSound = soundPool.load(this, R.raw.camera_click, 0);

		buttonLayout = (RelativeLayout)findViewById(R.id.button_layout);
		shutterButton = (ImageButton)findViewById(R.id.shutter_button);
		colorButton = (ImageButton)findViewById(R.id.color_button);
		fontButton = (ImageButton)findViewById(R.id.font_button);
		importButton = (ImageButton)findViewById(R.id.import_button);
		
		shutterButton.setOnClickListener(this);
		colorButton.setOnClickListener(this);
		fontButton.setOnClickListener(this);
		importButton.setOnClickListener(this);
		
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		
		//Used in onResume & onStop
		OrientationLstr mOrientationListener = new OrientationLstr(this);
		
		shutterLayout = (RelativeLayout)findViewById(R.id.shutter_layout);
		
    }

    /**
     * Reset all flags to their starting values.
     */
    private void resetFlags()	{
    	mIsPictureTaking	= false;
        mIsPreviewRunning	= false;
        mIsFocusStarted		= false;
        mIsFocusForPicture	= false;
        mIsFocused			= false;
        mIsMoving			= false;
        
        buttonLayout.setVisibility(View.VISIBLE);
    }
    
    /**
     * Sets the camera to rotate the image upon capturing it
     * in order to compensate for the user holding the device
     * in portrait or landscape modes.
     * @param ori
     */
    private void compensateForOrientation(int ori)	{	
    	Camera.Parameters params = mCamera.getParameters();
    	
    	if (ori == ORI_LAND_UP) params.setRotation(0);    	
    	else if (ori == ORI_PORT_UP) params.setRotation(90);
    	else if (ori == ORI_PORT_DOWN) params.setRotation(270);
    	else params.setRotation(180);
    	
    	mCamera.setParameters(params);
    }

    /////////////////begin zoom code ////////////////////////
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if(!mIsPictureTaking && mIsPreviewRunning)	{
		    switch (event.getAction() & MotionEvent.ACTION_MASK) {
		      
			case MotionEvent.ACTION_DOWN:
				buttonLayout.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_out));
		        buttonLayout.setVisibility(View.INVISIBLE);
		        
		        oldX = event.getX();
		        oldY = event.getY();
		        break;

			case MotionEvent.ACTION_UP:
				buttonLayout.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
		        buttonLayout.setVisibility(View.VISIBLE);

		         if(!mIsFocusStarted)	{
		        	mCamera.autoFocus(mAutoFocusCallback);
		         	mIsFocusStarted = true;
		         }
		         break;
		      
			case MotionEvent.ACTION_MOVE:
				if ((Math.abs(event.getX() - oldX)) >  (Math.abs(event.getY() - oldY))) { //Delta x > Delta y: use change in x for zoom
					if (mCurrentOrientation == ORI_LAND_UP) {
						if (event.getX() + 5 < oldX)
							zoom(ZOOM_OUT);
						else if (event.getX() > oldX + 5)
							zoom(ZOOM_IN);
					}
					
					else if (mCurrentOrientation == ORI_PORT_DOWN) {
						if (event.getX() + 5 < oldX)
							zoom(ZOOM_OUT);
						else if (event.getX() > oldX + 5)
							zoom(ZOOM_IN);
					}
					
					else {
						if (event.getX() + 5 < oldX)
							zoom(ZOOM_IN);
						else if (event.getX() > oldX + 5)
							zoom(ZOOM_OUT);
					}
				}
				
				else { //use change in y for zoom
					if (mCurrentOrientation == ORI_LAND_DOWN) {
						if (event.getY() > oldY + 5)
							zoom(ZOOM_IN);
			    		else if (event.getY() + 5 < oldY)
			    			zoom(ZOOM_OUT);
					}
					
					else if (mCurrentOrientation == ORI_PORT_DOWN) {
						if (event.getY() + 5 < oldY)
							zoom(ZOOM_OUT);
						else if (event.getY() > oldY + 5)
							zoom(ZOOM_IN);
					}
					
					else {
						if (event.getY() + 5 < oldY)
							zoom(ZOOM_IN);
			    		else if (event.getY() > oldY + 5)
			    			zoom(ZOOM_OUT);
					}
				}
				//Conceptually this is incorrect, but it works(looks nice too).
				oldX = event.getX();
				oldY = event.getY();
				break;
		    }
		    return true;
		}
		return false;
	}
	
	/**This code allows user to control zoom with volume controls**/
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if(!mIsPictureTaking && mIsPreviewRunning)	{
			int action = event.getAction();
		    int keyCode = event.getKeyCode();
		   
	        switch (keyCode) {
	        
	        case KeyEvent.KEYCODE_VOLUME_UP:
	            if (action == KeyEvent.ACTION_DOWN) {
	                zoom(ZOOM_IN);
	                return true;

	            }
	            if(action == KeyEvent.ACTION_UP)	{
	            	if(!mIsFocusStarted)	{
	            		mCamera.autoFocus(mAutoFocusCallback);
	            		mIsFocusStarted = true;
	            	}
	            }
	            return true;

	        case KeyEvent.KEYCODE_VOLUME_DOWN:
	            if (action == KeyEvent.ACTION_DOWN) {
	            	zoom(ZOOM_OUT);
	            }
	            if(action == KeyEvent.ACTION_UP)	{
	            	if(!mIsFocusStarted)	{
	            		mCamera.autoFocus(mAutoFocusCallback);
	            		mIsFocusStarted = true;
	            	}
	            }
	  			return true;
	  			
	        default:
	            return super.dispatchKeyEvent(event);
	        }
		}
		return false;
	}
	
	/**
	 * Handles camera zoom.
	 * @param direction The direction zooming should be applied.
	 */
	private void zoom(int direction)	{
		if(mIsPreviewRunning && !mIsPictureTaking)	{
			Camera.Parameters parameters = mCamera.getParameters();
			mCamera.cancelAutoFocus(); //No point in focusing, it'll just be out of focus.
			
			switch(direction)	{
			case ZOOM_IN:
				if (parameters.getZoom() < parameters.getMaxZoom()) adjustedZoomValue++;
				break;
			case ZOOM_OUT:
				if (parameters.getZoom() > 0) adjustedZoomValue--;
				break;
			}
			
			if (parameters.isSmoothZoomSupported())	{
				mCamera.startSmoothZoom(adjustedZoomValue);
			}else{
				parameters.setZoom(adjustedZoomValue);
				mCamera.setParameters(parameters);
			}
			mIsFocusStarted = false;//Allow focusing again.
		}
	}

	   /////////////////// end zoom code ////////////////////////
	
    /**
     * Passes the Uri of an image to ImageTextSelect.
     */
    private void passUriToITS(Uri imageUri)	{
		Log.d(TAG, "Passing Uri to ImageTextSelect");
    	Intent intent = new Intent(CameraPreview.this, ImageTextSelect.class);
		intent.putExtra(Intent.EXTRA_STREAM, imageUri);
		startActivity(intent);
	}
 
    
    /**
     * Save the given image data byte[] to a file in the phone's gallery.
     * 
     * @param imageData The byte array representing the image to be saved.
     * @return The Uri of the newly saved image, null if no SD card is mounted.
     */
    private Uri saveImage(byte[] imageData)	{
    	Log.d(TAG, "Attempting image save.");
		
    	if(!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){ //No SD card mounted
		    Toast.makeText(CameraPreview.this, "External SD card not mounted", Toast.LENGTH_LONG).show();
		    mCamera.startPreview();
		    mIsPreviewRunning = true;
		    return null;
		}
		
		//Number images to avoid overwrite
    	int nextPhotoNumber = preferences.getInt("nextPhotoNumber", 0);
		String fileName = "LenteImage" + nextPhotoNumber + ".jpg";
		
		
    	File path = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath() + IMAGE_DIR);
    	File file = new File(path, fileName);
    	
    	//This will only run if the user deleted their app data for Lente
    	//since that means the image count will have been reset.
    	while(file.exists())	{	//This avoids overwriting if preferences data is lost.
    		nextPhotoNumber++;
    		fileName = "LenteImage" + nextPhotoNumber + ".jpg";
    		file = new File(path, fileName); //LenteImage0.jpg
    	}
    	
    	//Allow Android to reclaim memory and allocate a big chunk for high res photo
    	BitmapFactory.Options bfOptions = new BitmapFactory.Options();
    	bfOptions.inPurgeable = true;
    	bfOptions.inTempStorage = new byte[16384]; //16Kb
    	
    	
    	try	{
    		Log.d(TAG, "Attempting to write...");
    		path.mkdir(); //Generate the directory if it doesn't exist
    		OutputStream os = new BufferedOutputStream(new FileOutputStream(file));
    		//Create a bitmap out off the image data, compress it, write it
    		BitmapFactory.decodeByteArray(imageData, 0, imageData.length, bfOptions).compress(Bitmap.CompressFormat.JPEG, 100, os);
    		
    		os.flush();
    		os.close();
    		Log.d(TAG, "Write successful!");
    		
    	} catch(IOException e)	{
    		Log.e(TAG, "saveImage: " + e.getMessage());
    		e.printStackTrace();
    		Toast.makeText(this, "Image failed to save: " + e.getMessage(), 4000).show();
    	}
    	
		//Update photo numbering to reflect a successful save
		nextPhotoNumber++;
		SharedPreferences.Editor editor = preferences.edit();
	    editor.putInt(NEXT_PHOTO_KEY, nextPhotoNumber);
	    editor.commit();
	    
		//Tell Android there is a new image, also gives back Uri of new image
		MediaScannerConnection.scanFile(this,
                new String[] { file.toString() }, null,
                new MediaScannerConnection.OnScanCompletedListener() {
            public void onScanCompleted(String path, Uri uri) {} //Media content providers now aware of new image
		});
    	
    	return Uri.parse(file.toURI().toString()); //Both URI and Android's Uri follow the RFC 2396 standard
    }

	/**
	 * Obtains the highest resolution preview size that fits on the screen
	 * from the device's list of supported preview sizes.
	 */
	private Camera.Size getBestPreviewSize(int width, int height, Camera.Parameters params)	{
		Log.d(TAG, "Getting optimal preview size...");
		Camera.Size size = null;
		
		//Obtain a size that is at most the size of the screen.
		for(Size s : params.getSupportedPreviewSizes()){
			if(s.width <= width && s.height <= height)	{
				if (size == null) {
					size = s;
				} else {
					int resultArea = size.width * size.height;
					int newArea = s.width * s.height;
				
					if (newArea>resultArea) { //If resolution is higher, use it
						size=s;
					}
				}
			}
		}
		return size;
	}
	
	/**
	 * Attempts to take a picture.
	 */
	private void takePicture()	{
		Log.d(TAG, "Attempting to take picture...");
		mCamera.cancelAutoFocus(); //Kill any current focusing
		mIsFocusStarted = false;
		mIsPictureTaking = true; //Set flag to lock out further attempts to
								//take pictures or focus.
		try	{
			mCamera.takePicture(mShutterCallback, null, mJpegPictureCallback);
			mIsPreviewRunning = false;
		} catch(RuntimeException e)	{
			//Camera fails, restart the app. Crude but effective.
			Toast.makeText(this, "Opps... Failed to take picture, try again.", 3000).show();
			Intent restartInt = new Intent(this, CameraPreview.class);
			restartInt.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); //Clear all other activities associated with the app.
			startActivity(restartInt);
		}
	}
    
    /**
     * Once an image has been received from the gallery hand it off to ImageTextSelect
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "Result received from the gallery...");
    	if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                passUriToITS(data.getData());
            }
        }
    }
    
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	/**
	 * Handles auto focus when device is moved in 3D space.
	 */
	@Override
	//curious side effect to using accelerometer...
	//If user maintains a steady speed, it will not register as movement.
	//So, if user is in a car, the camera will still try to focus if speed is maintained.
	//Additionally, this will work in SPACE!
	public void onSensorChanged(SensorEvent event) {
		if(mIsPreviewRunning && !mIsPictureTaking && !mIsFocusStarted)	{
			if(!mIsMoving && !mIsFocused)	{ //Not moving and not focused
				mCamera.autoFocus(mAutoFocusCallback);
				mIsFocusStarted = true;
			}
			
			mIsMoving = false;
			
			//Check difference in directional force.
			if(mAccX - event.values[0] > ACC_THRESH)	{
				Log.d(TAG, "Moving along X");
				mIsFocused = false; //Movement, no longer focused.
				mIsMoving = true; //Could still be moving.
			}
			if(mAccY - event.values[1] > ACC_THRESH)	{
				Log.d(TAG, "Moving along Y");
				mIsFocused = false;
				mIsMoving = true;
			}
			if(mAccZ - event.values[2] > ACC_THRESH)	{
				Log.d(TAG, "Moving along Z");
				mIsFocused = false;
				mIsMoving = true;
			}
			
			//Update values to be used as next reference.
			mAccX = event.values[0];
			mAccY = event.values[1];
			mAccZ = event.values[2];
			
		}
	}
    
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Log.d(TAG, "onSavedInstanceState");
	}
	
	protected void onStart()	{
		super.onStart();
		Log.d(TAG, "onStart");
	}
	
	protected void onRestart()	{
		super.onRestart();
		Log.d(TAG, "onRestart");
	}
	
	protected void onResume()	{
		super.onResume();
		Log.d(TAG, "onResume");
		resetFlags();
		mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
		mOrientationListener = new OrientationLstr(this);
		mOrientationListener.enable();
	}
	
	protected void onPause()	{
		super.onPause();
		Log.d(TAG, "onPause");
		mSensorManager.unregisterListener(this);
		
		if(mOrientationListener != null)
			mOrientationListener.disable();
	}
	
	protected void onStop()	{
		super.onStop();
		Log.d(TAG, "onStop");
	}
	
	protected void onDestroy()	{
		super.onDestroy();
		Log.d(TAG, "onDestroy");
	}
	
	/**
	 * Called when auto focus is used, successful or not.
	 * Will take a picture if mIsFocusForPicture is true.
	 */
	private AutoFocusCallback mAutoFocusCallback = new AutoFocusCallback()	{
		@Override
		public void onAutoFocus(boolean success, Camera camera) {
			mIsFocusStarted = false;
			mIsFocused = true;
			
			if(success && mIsFocusForPicture)	{
				takePicture();
			}
			mIsFocusForPicture = false;
		}
	};
	
	/**
	 * Called when a picture is taken and sends the Uri of the newly captured image
	 * onto ImageTextSelect.
	 */
	private PictureCallback mJpegPictureCallback = new PictureCallback()	{
		@Override
		public void onPictureTaken(byte[] imageData, Camera camera) {
			mIsPictureTaking = false;

			if(imageData != null) {
				Log.d(TAG, "Picture taken successfully!");
				passUriToITS(saveImage(imageData)); //Save and pass the image
			}
		}
	};
	
	/**
	 * Called when the camera shutter is activated.
	 */
	private ShutterCallback mShutterCallback = new ShutterCallback() {
		@Override
		public void onShutter() {
			soundPool.play(shutterSound, 1f, 1f, 0, 0, 1);
		}
	};

	@Override
	public void onClick(View v) {
		Intent buttonIntent = new Intent();
		switch(v.getId()) {
		
		case R.id.color_button:
			buttonIntent = new Intent(CameraPreview.this, ColorSettings.class);	
    		startActivity(buttonIntent);
    		return;
		case R.id.shutter_button:
			synchronized(this)	{
				if(mIsPreviewRunning)	{//Can only take a picture if the preview is running
					if(!mIsPictureTaking)	{//Only one pic at a time
						mCamera.cancelAutoFocus();
						mIsFocusStarted = false;
						Log.d(TAG, "Taking picture");
						compensateForOrientation(mCurrentOrientation);
						Log.d(TAG, "mode=Trying");
						mIsFocusForPicture = true;//Let focus know we want a picture taken
						mCamera.autoFocus(mAutoFocusCallback);
						mIsFocusStarted = true;
					}
				}
			}
			return;
		case R.id.font_button:
			buttonIntent = new Intent(CameraPreview.this, FontSettings.class);	
    		startActivity(buttonIntent);
    		return;
		case R.id.import_button:
			buttonIntent = new Intent(Intent.ACTION_GET_CONTENT);
            buttonIntent.setType("image/*");
            startActivityForResult(Intent.createChooser(buttonIntent, "Please Select Picture"), SELECT_PICTURE);
            return;
		default:
    		return;
    	
		}
	}

	/**
	 * Handles Surface life cycle events.
	 */
	private class SurfaceHolderCb implements SurfaceHolder.Callback	{
		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			Log.d(TAG, "Changing surface...");
			if(mIsPreviewRunning)	{
				mCamera.stopPreview();
				mIsPreviewRunning = false;
			}
			
			Camera.Parameters parameters = mCamera.getParameters();
		    Camera.Size previewSize = getBestPreviewSize(width, height, parameters);
		    
		    if(previewSize != null)	{
		    	Log.d(TAG, "Setting camera parameters");
		    	parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
		    	parameters.setPreviewSize(previewSize.width, previewSize.height);
		    	parameters.setPictureFormat(PixelFormat.JPEG);
		    	mCamera.setParameters(parameters);
		    	
		    	mCamera.startPreview();
		    	mIsPreviewRunning = true;
		    }
		}
	
		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			Log.d(TAG, "Creating surface");
			//Gain control of the camera.
			mCamera = Camera.open();
			
			try {
	    		mCamera.setPreviewDisplay(mSurfaceHolder);
	    	} catch (IOException e) {
	    		e.printStackTrace();
	    		Log.e(TAG, "Surface creation failed");
	    	}
		}
	
		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			Log.d(TAG, "Surface destroyed");
			//Surface is gone, stop the preview and let go of the camera
			if(mCamera != null)	{
				if(mIsPreviewRunning)	{
					mCamera.stopPreview();
					mIsPreviewRunning = false;
				}
				mCamera.release();
				mCamera = null;
			}
		}
    }
	
	/**
	 * Returns a value representing how the device is being held.
	 * ORI_LAND or ORI_PORT.
	 */
	public class OrientationLstr extends OrientationEventListener{
		public OrientationLstr(Context context) {
			super(context);
		}

		@Override
		public void onOrientationChanged(int orientation) {			
			if (orientation <= 315 && orientation >= 225)	{ //upright landscape
				if (lastOrientation!=ORI_LAND_UP) {
					anim = AnimationUtils.loadAnimation(CameraPreview.this, R.anim.shutter_button_go_land_up);
					anim.setAnimationListener(new AnimationListener() {
						
						@Override
						public void onAnimationStart(Animation paramAnimation) {
							lastOrientation = ORI_LAND_UP;
						}
						@Override
						public void onAnimationRepeat(Animation paramAnimation) {						
						}
						@Override
						public void onAnimationEnd(Animation paramAnimation) {
						}
						});
					anim.setFillAfter(true);
				}
				
				if (mCurrentOrientation == ORI_PORT_DOWN) {
					fromDegrees = toDegrees;
					toDegrees-=90;
					rotateButtons();
					rotateShutter();
				}
				else if (mCurrentOrientation == ORI_PORT_UP) {
					fromDegrees = toDegrees;
					toDegrees+=90;
					rotateButtons();
					rotateShutter();
				}
				else if (mCurrentOrientation == ORI_LAND_DOWN) {
					fromDegrees = toDegrees;
					toDegrees+=180;
					rotateButtons();
					rotateShutter();
				}
				mCurrentOrientation = ORI_LAND_UP;
			}
			
			if (orientation > 315 || orientation < 45)	{ //upright portrait
				
				if ((lastOrientation!=ORI_PORT_UP) && (lastOrientation!=ORI_PORT_DOWN)) {
					if (lastOrientation == ORI_LAND_UP)
						anim = AnimationUtils.loadAnimation(CameraPreview.this, R.anim.shutter_button_center_from_land_up);
					else
						anim = AnimationUtils.loadAnimation(CameraPreview.this, R.anim.shutter_button_center_from_land_down);
					anim.setAnimationListener(new AnimationListener() {
						
						@Override
						public void onAnimationStart(Animation paramAnimation) {
							lastOrientation = ORI_PORT_UP;
						}
						@Override
						public void onAnimationRepeat(Animation paramAnimation) {						
						}
						@Override
						public void onAnimationEnd(Animation paramAnimation) {
						}
						});
					anim.setFillAfter(true);
				}
					
					if (mCurrentOrientation == ORI_LAND_UP) {
						fromDegrees = toDegrees;
						toDegrees-=90;
						rotateButtons();
						rotateShutter();
					}
					else if (mCurrentOrientation == ORI_LAND_DOWN) {
						fromDegrees = toDegrees;
						toDegrees+=90;
						rotateButtons();
						rotateShutter();
					}
					else if (mCurrentOrientation == ORI_PORT_DOWN) {
						fromDegrees = toDegrees;
						toDegrees+=180;
						rotateButtons();
						rotateShutter();
					}
				mCurrentOrientation = ORI_PORT_UP;
			}
		
			if (orientation >= 45 && orientation <= 135) { //upside-down landscape
				if (lastOrientation!=ORI_LAND_DOWN) {
					Log.d(TAG, "ORI_LAND_DOWN");
					anim = AnimationUtils.loadAnimation(CameraPreview.this, R.anim.shutter_button_go_land_down);
					anim.setAnimationListener(new AnimationListener() {
						
						@Override
						public void onAnimationStart(Animation paramAnimation) {
							lastOrientation = ORI_LAND_DOWN;
						}
						
						@Override
						public void onAnimationRepeat(Animation paramAnimation) {							
						}
						
						@Override
						public void onAnimationEnd(Animation paramAnimation) {
						}
					});
					anim.setFillAfter(true);
				}
					if (mCurrentOrientation == ORI_PORT_UP) {
						fromDegrees = toDegrees;
						toDegrees-=90;
						rotateButtons();
						rotateShutter();
					}
					else if (mCurrentOrientation == ORI_PORT_DOWN) {
						fromDegrees = toDegrees;
						toDegrees+=90;
						rotateButtons();
						rotateShutter();
					}
					else if (mCurrentOrientation == ORI_LAND_UP) {
						Log.d(TAG, "rotate 180");
						fromDegrees = toDegrees;
						toDegrees+=180;
						rotateButtons();
						rotateShutter();
					}
				mCurrentOrientation = ORI_LAND_DOWN;   
			}
			
			if (orientation > 135 && orientation < 225) { //upside-down portrait
				
				if (lastOrientation!=ORI_PORT_UP && lastOrientation!=ORI_PORT_DOWN) {
					if (mCurrentOrientation == ORI_LAND_UP)
						anim = AnimationUtils.loadAnimation(CameraPreview.this, R.anim.shutter_button_center_from_land_up);
					else
						anim = AnimationUtils.loadAnimation(CameraPreview.this, R.anim.shutter_button_center_from_land_down);
					anim.setAnimationListener(new AnimationListener() {
						
						@Override
						public void onAnimationStart(Animation paramAnimation) {
							lastOrientation = ORI_PORT_DOWN;
						}
						@Override
						public void onAnimationRepeat(Animation paramAnimation) {						
						}
						@Override
						public void onAnimationEnd(Animation paramAnimation) {
						}
						});
					
					anim.setFillAfter(true);
				}
					if (mCurrentOrientation == ORI_LAND_DOWN) {
						fromDegrees = toDegrees;
						toDegrees-=90;
						rotateButtons();
						rotateShutter();
					}
					else if (mCurrentOrientation == ORI_LAND_UP) {
						fromDegrees = toDegrees;
						toDegrees+=90;
						rotateButtons();
						rotateShutter();
					}
					else if (mCurrentOrientation == ORI_PORT_UP) {
						fromDegrees = toDegrees;
						toDegrees+=180;
						rotateButtons();
						rotateShutter();
				}
				mCurrentOrientation = ORI_PORT_DOWN;
			}
		}
	}
		private void rotateButtons() {
			Log.d(TAG, "rotateButton()");
			rotate = new RotateAnimation(fromDegrees, toDegrees, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
	    	rotate.setDuration(250);
			rotate.setFillAfter(true);
			
			importButton.startAnimation(rotate);
			colorButton.startAnimation(rotate);
			fontButton.startAnimation(rotate);
		}
			
		private void rotateShutter() {
			Log.d(TAG, "rotateShutter()");
			rotate = new RotateAnimation(fromDegrees, toDegrees, 
					Animation.ABSOLUTE, shutterButton.getWidth() / 2, 
					Animation.ABSOLUTE, shutterButton.getHeight() / 2);
			rotate.setDuration(250);
			rotate.setFillAfter(true);
			if (anim != null)
				shutterLayout.startAnimation(anim);
			shutterButton.startAnimation(rotate);
		}
}