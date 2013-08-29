package df.lente;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.webkit.WebView;

/**
 * Customized WebView, built specifically to control button visibility in ImageTextSelect.
 * @author Eric Ostrowski
 */
public class TextSelectWebView extends WebView {

	private Hideable caller;
	
	public TextSelectWebView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public TextSelectWebView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public TextSelectWebView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}
	
	public boolean onTouchEvent(MotionEvent e, Hideable caller)	{
		this.caller = caller;
		return onTouchEvent(e);
	}
	
	public boolean onTouchEvent(MotionEvent e)	{
		if(e.getAction() == MotionEvent.ACTION_DOWN)	{
			caller.hideViews();
		}else if(e.getAction() == MotionEvent.ACTION_UP)	{
			caller.showViews();
		}
		return super.onTouchEvent(e);
	}

}