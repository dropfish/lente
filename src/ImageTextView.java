package df.lente;

import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import df.lente.R;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.text.ClipboardManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ImageTextView extends Activity implements TextToSpeech.OnInitListener, Hideable, OnTouchListener {
	
	private RelativeLayout buttonLayout;
	private static final String TAG = "ImageTextView";
	private TextToSpeech mTts;
    private ImageButton playButton;
    private ImageButton stopButton;
    
    private int textColor = Color.RED;
    private int textSize = 7;
    
    private String textString = "This is a long scrollable message...";
	
    private ClipboardManager clipboard;
    private ImageButton copyButton;
    
    private TextSelectWebView webView;
    
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_text_view);
        buttonLayout = (RelativeLayout)findViewById(R.id.image_text_view_button_layout);
        webView = (TextSelectWebView)findViewById(R.id.image_text_view_web_view);
        
        clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        
        mTts = new TextToSpeech(this, this);
        
        stopButton = (ImageButton) findViewById(R.id.stop_button);
        stopButton.setVisibility(View.INVISIBLE);
        playButton = (ImageButton) findViewById(R.id.play_button);
        copyButton = (ImageButton) findViewById(R.id.copy_button); 
        playButton.bringToFront();
        
        playButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	mTts.speak(textString, TextToSpeech.QUEUE_FLUSH, null);
            	playButton.setVisibility(View.INVISIBLE);
            	stopButton.setVisibility(View.VISIBLE);
            	stopButton.bringToFront();
            }
        });
        stopButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	stopSpeech();
            }
        });
        copyButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	clipboard.setText(textString);
            	Toast.makeText(getApplicationContext(), "copied to clipboard: " + clipboard.getText(), 4000).show();
            }
        });
        
        webView.setOnTouchListener(this);
        
        displayText();
	}
	
	public void stopSpeech() {
		mTts.stop();
    	playButton.setVisibility(View.VISIBLE);
    	playButton.bringToFront();
    	stopButton.setVisibility(View.INVISIBLE);
	}

	public void hideViews()	{
		Log.d(TAG, "Hiding buttons");
		buttonLayout.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_out));
		buttonLayout.setVisibility(View.INVISIBLE);
	}
	
	public void showViews()	{
		Log.d(TAG, "Showing buttons");
		buttonLayout.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
		buttonLayout.setVisibility(View.VISIBLE);
	}
	
	public boolean onTouch(View v, MotionEvent event) {
		Log.d(TAG, "onTouch");
		return webView.onTouchEvent(event, this);
	}
	
	private void displayText()	{
		Log.d(TAG, "displayText");
		webView.loadData("<html><body><p><font size=\"" + 
				textSize + "\" color=\"" + textColor+ "\">" + textString + "</font></p></body></html>",
				"text/html", "UTF-8");
		webView.getSettings().setBuiltInZoomControls(true);
		webView.setInitialScale(200);
	}
	
	public void onInit(int status) {
        // status can be either TextToSpeech.SUCCESS or TextToSpeech.ERROR.
        if (status == TextToSpeech.SUCCESS) {
            // Set preferred language to US english.
            // Note that a language may not be available, and the result will indicate this.
            int result = mTts.setLanguage(Locale.US);
            // Try this someday for some interesting results.
            // int result mTts.setLanguage(Locale.FRANCE);
            if (result == TextToSpeech.LANG_MISSING_DATA ||
                result == TextToSpeech.LANG_NOT_SUPPORTED) {
               // Lanuage data is missing or the language is not supported.
                Log.e(TAG, "Language is not available.");
            } else {
                // Check the documentation for other possible result codes.
                // For example, the language may be available for the locale,
                // but not for the specified country and variant.

                // The TTS engine has been successfully initialized.
                // Allow the user to press the button for the app to speak again.
                playButton.setEnabled(true);
            }
        } else {
            // Initialization failed.
            Log.e(TAG, "Could not initialize TextToSpeech.");
        }
    }

	@Override
	public void onDestroy() {
		// Don't forget to shutdown!
	    if (mTts != null) {
	    	mTts.stop();
	        mTts.shutdown();
	    }

	    super.onDestroy();
	}
}