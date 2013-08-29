package df.lente;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.Toast;

public class ImageTextSelect extends Activity implements OnTouchListener, Hideable {
	
	private static final String TAG = "ImageTextSelect";
	
	private Uri mImageUri;
	private TextSelectWebView mCropView;
	private ImageButton mSendButton;

	public void onCreate(Bundle bundle)	{
		super.onCreate(bundle);
		Log.d(TAG, "onCreate");
		setContentView(R.layout.image_text_select);
		

	    
	    mCropView = (TextSelectWebView) findViewById(R.id.webview);
		mCropView.setOnTouchListener(this);
	    
	    mSendButton = (ImageButton)findViewById(R.id.send_button);
	    mSendButton.setOnClickListener(new OnClickListener() {
	    	public void onClick(View v) {
	    		savePicture();
//	    		Intent intent = new Intent(ImageTextSelect.this, ImageTextView.class);	
//	    		startActivity(intent);
	    		Toast.makeText(getApplicationContext(), "Picture saved to Lente... Sending you to OCRopus", 2500).show();
	    	}
	    });
	}

	//This is only called once or so. The WebView webkit engine takes over
	//and succeeding calls.
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		Log.d(TAG, "onTouch");
		return mCropView.onTouchEvent(event, this);
	}
	
	/**
	 * Loads the Bitmap from the specified Uri into the WebView
	 * @param uri The Uri of the desired image to be loaded.
	 */
	private void displayImage(Uri uri)	{
		Log.d(TAG, "displayImage");
		int width = getWindow().getWindowManager().getDefaultDisplay().getWidth();
		int height = getWindow().getWindowManager().getDefaultDisplay().getHeight();
		String imageWidth = "\" width=\"" + width;
		String imageHeight = "\" height=\"" + height;
		
		if (width < height) //use width but not height, so set height to null
			imageHeight = "";
		else //use height not width, so set width to null
			imageWidth = "";
				
		String imageUrl = "file://" + uri.getPath();
		Log.d(TAG, "Loading image...");
		mCropView.loadData("<html><head>" +
				"<meta name=\"viewport\" content=\"width=device-width\"/>" +
						"</head><body><center><img src=\""+uri.toString() + imageWidth + imageHeight +
						"\"></center></body></html>",
						"text/html", "UTF-8");
		mCropView.getSettings().setBuiltInZoomControls(true);
		mCropView.setBackgroundColor(0);
		mCropView.getSettings().setUseWideViewPort(true);
		mCropView.setInitialScale(1);
		mCropView.getSettings().setLoadWithOverviewMode(true);
		Log.d(TAG, imageUrl);
		Log.d(TAG, uri.toString());
	}
	
	/**
	 * Converts a byte array of raw image data into a usable Bitmap.
	 */
	
	public void savePicture() {
		Log.d(TAG, "savePicture");
		
		File cropFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath() + "/Lente/crop.jpg");
		mCropView.setDrawingCacheEnabled(true);
		Bitmap bm = Bitmap.createBitmap(mCropView.getDrawingCache());
		mCropView.setDrawingCacheEnabled(false);
		
		try {
		    FileOutputStream outputStream = new FileOutputStream(cropFile);
		    Log.d(TAG, "Compressing and saving...");
		    bm.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
		    outputStream.flush();
		    outputStream.close();
		    
		    MediaScannerConnection.scanFile(getBaseContext(),
	                new String[] { cropFile.toString() }, null,
	                new MediaScannerConnection.OnScanCompletedListener() {
	            public void onScanCompleted(String path, Uri uri) {
	                Log.i("ExternalStorage", "Scanned " + path + ":");
	                Log.i("ExternalStorage", "-> uri=" + uri);
	                //passUri(uri); //Pass the URI once obtained and move onto OCRopus
	            }
			});
	    }
		
		catch (IOException e) {
			Toast.makeText(ImageTextSelect.this, "Failed to save selected image area.", 2000).show();
			Log.e(TAG, "IO Exception");
		}
	}
	
	public void hideViews()	{
		Log.d(TAG, "Hiding buttons");
		mSendButton.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_out));
		mSendButton.setVisibility(View.INVISIBLE);
	}
	
	public void showViews()	{
		Log.d(TAG, "Showing buttons");
		mSendButton.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
		mSendButton.setVisibility(View.VISIBLE);
	}
	
	protected void onStart()	{
		Log.d(TAG, "onStart");
		super.onStart();
	}
	
	protected void onRestart()	{
		Log.d(TAG, "onRestart");
		super.onRestart();
	}
	
	protected void onResume()	{
		Log.d(TAG, "onResume");
		super.onResume();
		
		Intent intent = getIntent();
	    Bundle extras = intent.getExtras();
	    
		if (extras.containsKey(Intent.EXTRA_STREAM))	{
			mImageUri = (Uri)extras.getParcelable(Intent.EXTRA_STREAM);
			displayImage(mImageUri);
		}
	}
	
	protected void onPause()	{
		Log.d(TAG, "onPause");
		super.onPause();
		mCropView.freeMemory();
		mCropView.clearView();
	}
	
	protected void onStop()	{
		Log.d(TAG, "onStop");
		super.onStop();
	}
	
	protected void onDestroy()	{
		Log.d(TAG, "onDestroy");
		super.onDestroy();
		System.gc();
	}
}