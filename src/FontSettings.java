package df.lente;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import df.lente.R;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

public class FontSettings extends Activity implements SeekBar.OnSeekBarChangeListener {
	
	SharedPreferences preferences;
	private int alphabeticFontInteger;
	private int alphabeticTextColorInteger;
	private int alphabeticBackgroundColorInteger;
	private int textColor;
	private int backgroundColor;
	private RelativeLayout layout;
	private int progress;
	private int textSize;
	private Typeface font;
	
	Typeface arial;
	Typeface verdana;
	Typeface microsoft;
	Typeface tahoma;
	Typeface gothic;
	Typeface georgia;
	
	TextView textPreview;	
	SeekBar seekBar;
	
	Button arial_button;
	Button verdana_button;
	Button microsoft_button;
	Button tahoma_button;
	Button gothic_button;
	Button georgia_button;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.font_layout);
        
        arial_button = (Button)findViewById(R.id.arial_button);
    	verdana_button = (Button)findViewById(R.id.verdana_button);
    	microsoft_button = (Button)findViewById(R.id.microsoft_button);
    	tahoma_button = (Button)findViewById(R.id.tahoma_button);
    	gothic_button = (Button)findViewById(R.id.gothic_button);
    	georgia_button = (Button)findViewById(R.id.georgia_button);
        
        arial = Typeface.createFromAsset(getAssets(), "fonts/ARIBLK.TTF");
        verdana = Typeface.createFromAsset(getAssets(), "fonts/VERDANA.TTF");
        microsoft = Typeface.createFromAsset(getAssets(), "fonts/MICROSS.TTF");
        tahoma = Typeface.createFromAsset(getAssets(), "fonts/TAHOMA.TTF");
        gothic = Typeface.createFromAsset(getAssets(), "fonts/GOTHIC.TTF");
        georgia = Typeface.createFromAsset(getAssets(), "fonts/GEORGIA.TTF");
        
        arial_button.setTypeface(arial);
        verdana_button.setTypeface(verdana);
        microsoft_button.setTypeface(microsoft);
        tahoma_button.setTypeface(tahoma);
        gothic_button.setTypeface(gothic);
        georgia_button.setTypeface(georgia);
        
        arial_button.setOnClickListener(fontListener);
        verdana_button.setOnClickListener(fontListener);
        microsoft_button.setOnClickListener(fontListener);
        tahoma_button.setOnClickListener(fontListener);
        gothic_button.setOnClickListener(fontListener);
        georgia_button.setOnClickListener(fontListener);
        
        //load settings from prefernces
        preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        
        alphabeticFontInteger = preferences.getInt("storedAlphabeticFontInteger", 1);
        textSize = preferences.getInt("storedTextSize", 45);
        progress = preferences.getInt("storedSeekBarProgress", 15);
        switch (alphabeticFontInteger){
        	case 1:
        		font = arial;
        		break;
        	case 2:
        		font = georgia;
        		break;
        	case 3:
        		font = gothic;
        		break;
        	case 4:
        		font = microsoft;
        		break;
        	case 5:
        		font = tahoma;
        		break;
        	case 6:
        		font = verdana;
        		break;
        	default:
        		break;
        		
        }
        
        alphabeticTextColorInteger = preferences.getInt("storedAlphabeticTextColorInteger", 1);
        alphabeticBackgroundColorInteger = preferences.getInt("storedAlphabeticBackgroundColorInteger", 4);
        
        switch (alphabeticTextColorInteger) {
        	case 1:
        		textColor = Color.BLACK;
        		break;
        	case 2:
        		textColor = getResources().getColor(R.color.dark_blue);;
        		break;
        	case 3:
        		textColor = getResources().getColor(R.color.dark_red);;
        		break;
        	case 4:
        		textColor = Color.WHITE;
        		break;
        	default:
        		break;
        }
        
        switch (alphabeticBackgroundColorInteger) {
    	case 1:
    		backgroundColor = Color.BLACK;
    		break;
    	case 2:
    		backgroundColor = getResources().getColor(R.color.light_blue);
    		break;
    	case 3:
    		backgroundColor = getResources().getColor(R.color.pink);
    		break;
    	case 4:
    		backgroundColor = Color.WHITE;
    		break;
    	default:
    		break;
    }
        
        textPreview = (TextView)findViewById(R.id.text_preview);
        textPreview.setTextSize(textSize);
        textPreview.setTypeface(font);
        textPreview.setTextColor(textColor);
        layout = (RelativeLayout)findViewById(R.id.font_layout);
        layout.setBackgroundColor(backgroundColor);
        
        seekBar = (SeekBar)findViewById(R.id.seekbar);
        seekBar.setProgress(progress);
        seekBar.setOnSeekBarChangeListener(this);
	}

	public void onProgressChanged(SeekBar paramSeekBar, int paramInt, boolean paramBoolean) {
		progress = paramInt;
		textSize = progress + 30;
		textPreview.setTextSize(textSize);
	}

	public void onStartTrackingTouch(SeekBar paramSeekBar) {
	}

	public void onStopTrackingTouch(SeekBar paramSeekBar) {
	}
	
	//begin onclicklisteners
	OnClickListener fontListener = new OnClickListener() {
    	public void onClick(View v) {
    		switch (v.getId()) {
	    		case R.id.arial_button:
	    			font = arial;
	    			alphabeticFontInteger = 1;
	    			break;
	    		case R.id.verdana_button:
	    			font = verdana;
	    			alphabeticFontInteger = 6;
	    			break;
	    		case R.id.microsoft_button:
	    			font = microsoft;
	    			alphabeticFontInteger = 4;
	    			break;
	    		case R.id.tahoma_button:
	    			font = tahoma;
	    			alphabeticFontInteger = 5;
	    			break;
	    		case R.id.gothic_button:
	    			font = gothic;
	    			alphabeticFontInteger = 3;
	    			break;
	    		case R.id.georgia_button:
	    			font = georgia;
	    			alphabeticFontInteger = 2;
	    			break;
	    		default:
	    			break;
    		}
    		textPreview.setTypeface(font);
    	}
    };
    
    protected void onStop(){
        super.onStop();

       SharedPreferences.Editor editor = preferences.edit();
       editor.putInt("storedAlphabeticFontInteger", alphabeticFontInteger);
       editor.putInt("storedTextSize", textSize);
       editor.putInt("storedSeekBarProgress", progress);
       editor.commit();
    }

}