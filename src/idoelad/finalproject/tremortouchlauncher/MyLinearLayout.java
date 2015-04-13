package idoelad.finalproject.tremortouchlauncher;

import android.content.Context;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class MyLinearLayout extends LinearLayout {
	public MyLinearLayout(Context context) {
		super(context);
	}

	public MyLinearLayout(Context context, AttributeSet attrs) {
		super(context,attrs);
	}

	public MyLinearLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context,attrs,defStyle);
	}



	@Override
	public boolean dispatchTouchEvent(MotionEvent e) {
		Log.i("@@", "Touch: REAL | T="+String.valueOf(e.getDownTime())+" | "+String.valueOf(e.getX())+","+String.valueOf(e.getRawY()));
		e.setLocation(e.getX()+100, e.getY());
		Log.i("@@", "Touch: FAKE |  T="+String.valueOf(e.getDownTime())+" | "+String.valueOf(e.getX())+","+String.valueOf(e.getRawY()));
		Log.i("@@","----------------------------------------");
		return super.dispatchTouchEvent(e);
	}
}
