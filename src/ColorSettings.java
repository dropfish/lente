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
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ColorSettings extends Activity {
	
	SharedPreferences preferences;
	
	private int alphabeticFontInteger;
	private int alphabeticTextColorInteger;
	private int alphabeticBackgroundColorInteger;
	private int textSize;
	private Typeface font;
	
	private int backgroundColor = Color.WHITE;
	private int textColor = Color.BLACK;

	TextView textHeader;
	TextView backgroundHeader;

	ImageButton blackText;
	ImageButton darkRed;
	ImageButton darkBlue;
	ImageButton whiteText;
	
	ImageButton lightBlue;
	ImageButton pink;
	ImageButton whiteBackground;
	ImageButton blackBackground;
	
	Typeface arial;
	Typeface verdana;
	Typeface microsoft;
	Typeface tahoma;
	Typeface gothic;
	Typeface georgia;
	
	RelativeLayout layout;
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.colors2);
        
        layout = (RelativeLayout)findViewById(R.id.color_layout);
        textHeader = (TextView)findViewById(R.id.text_header);
        backgroundHeader = (TextView)findViewById(R.id.background_header);
        
        preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        textSize = preferences.getInt("storedTextSize", 45);
        alphabeticTextColorInteger = preferences.getInt("storedAlphabeticTextColorInteger", 1);
        alphabeticBackgroundColorInteger = preferences.getInt("storedAlphabeticBackgroundColorInteger", 4);
        alphabeticFontInteger = preferences.getInt("storedAlphabeticFontInteger", 1);
        
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
        
        arial = Typeface.createFromAsset(getAssets(), "fonts/ARIBLK.TTF");
        verdana = Typeface.createFromAsset(getAssets(), "fonts/VERDANA.TTF");
        microsoft = Typeface.createFromAsset(getAssets(), "fonts/MICROSS.TTF");
        tahoma = Typeface.createFromAsset(getAssets(), "fonts/TAHOMA.TTF");
        gothic = Typeface.createFromAsset(getAssets(), "fonts/GOTHIC.TTF");
        georgia = Typeface.createFromAsset(getAssets(), "fonts/GEORGIA.TTF");
        
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
        
        layout.setBackgroundColor(backgroundColor);
        
        textHeader.setTextColor(textColor);
        textHeader.setTypeface(font);
        textHeader.setTextSize(textSize);
        backgroundHeader.setTextColor(textColor);
        backgroundHeader.setTypeface(font);
        backgroundHeader.setTextSize(textSize);
        
        //text
        blackText = (ImageButton)findViewById(R.id.black_text);
        darkRed = (ImageButton)findViewById(R.id.dark_red_swatch);
        darkBlue = (ImageButton)findViewById(R.id.dark_blue_swatch);
        whiteText = (ImageButton)findViewById(R.id.white_text);
        
        blackText.setOnClickListener(blackTextListener);
        darkRed.setOnClickListener(darkRedListener);
        darkBlue.setOnClickListener(darkBlueListener);
        whiteText.setOnClickListener(whiteTextListener);
        
        //background
        lightBlue = (ImageButton)findViewById(R.id.light_blue_swatch);
        pink = (ImageButton)findViewById(R.id.pink_swatch);
        whiteBackground = (ImageButton)findViewById(R.id.white_background);
        blackBackground = (ImageButton)findViewById(R.id.black_background);
        
        lightBlue.setOnClickListener(lightBlueListener);
        pink.setOnClickListener(pinkListener);
        whiteBackground.setOnClickListener(whiteBackgroundListener);
        blackBackground.setOnClickListener(blackBackgroundListener);
	}
	
	//begin text listeners
	OnClickListener blackTextListener = new OnClickListener() {
    	public void onClick(View v) {
    		if (backgroundColor == Color.BLACK) {
    			backgroundColor = Color.WHITE;
    			alphabeticBackgroundColorInteger = 4;
    			layout.setBackgroundColor(backgroundColor);
    		}
    			textColor = Color.BLACK;
    			alphabeticTextColorInteger = 1;
    			textHeader.setTextColor(textColor);
    			backgroundHeader.setTextColor(textColor);
    	}
    };
    
    OnClickListener darkRedListener = new OnClickListener() {
    	public void onClick(View v) {
    		textColor = getResources().getColor(R.color.dark_red);
    		alphabeticTextColorInteger = 3;
    		textHeader.setTextColor(textColor);
    		backgroundHeader.setTextColor(textColor);
    	}
    };
    
    OnClickListener darkBlueListener = new OnClickListener() {
    	public void onClick(View v) {
    		textColor = getResources().getColor(R.color.dark_blue);
    		alphabeticTextColorInteger = 2;
    		textHeader.setTextColor(textColor);
    		backgroundHeader.setTextColor(textColor);
    	}
    };
    
    OnClickListener whiteTextListener = new OnClickListener() {
    	public void onClick(View v) {
    		if (backgroundColor == Color.WHITE) {
    			backgroundColor = Color.BLACK;
    			alphabeticBackgroundColorInteger = 1;
    			layout.setBackgroundColor(backgroundColor);
    		}
    			textColor = Color.WHITE;
    			alphabeticTextColorInteger = 4;
    			textHeader.setTextColor(textColor);
    			backgroundHeader.setTextColor(textColor);
    	}
    };
   
    //begin background listeners
    OnClickListener lightBlueListener = new OnClickListener() {
    	public void onClick(View v) {
    		backgroundColor = getResources().getColor(R.color.light_blue);
    		alphabeticBackgroundColorInteger = 2;
    		layout.setBackgroundColor(backgroundColor);
    	}
    };
    
    OnClickListener pinkListener = new OnClickListener() {
    	public void onClick(View v) {
    		backgroundColor = getResources().getColor(R.color.pink);
    		alphabeticBackgroundColorInteger = 3;
    		layout.setBackgroundColor(backgroundColor);
    	}
    };
    
    OnClickListener whiteBackgroundListener = new OnClickListener() {
    	public void onClick(View v) {
    		if (textColor == Color.WHITE) {
    			textColor = Color.BLACK;
    			alphabeticTextColorInteger = 1;
    			textHeader.setTextColor(textColor);
    			backgroundHeader.setTextColor(textColor);
    		}
    			backgroundColor = Color.WHITE;
    			alphabeticBackgroundColorInteger = 4;
        		layout.setBackgroundColor(backgroundColor);
    	}
    };
    
    OnClickListener blackBackgroundListener = new OnClickListener() {
    	public void onClick(View v) {
    		if (textColor == Color.BLACK) {
    			textColor = Color.WHITE;
    			alphabeticTextColorInteger = 4;
    			textHeader.setTextColor(textColor);
    			backgroundHeader.setTextColor(textColor);
    		}
    			backgroundColor = Color.BLACK;
    			alphabeticBackgroundColorInteger = 1;
        		layout.setBackgroundColor(backgroundColor);
    	}
    };
    
    protected void onStop(){
       super.onStop();

       SharedPreferences.Editor editor = preferences.edit();
       editor.putInt("storedAlphabeticBackgroundColorInteger", alphabeticBackgroundColorInteger);
       editor.putInt("storedAlphabeticTextColorInteger", alphabeticTextColorInteger);
       editor.commit();
    }
}