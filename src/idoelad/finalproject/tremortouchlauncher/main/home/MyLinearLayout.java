package idoelad.finalproject.tremortouchlauncher.main.home;

import idoelad.finalproject.core.bigtouch.BigTouch;
import idoelad.finalproject.core.deviationtouch.DeviationTouch;
import idoelad.finalproject.core.multitouch.MultiTouch;
import idoelad.finalproject.core.touch.Circle;
import idoelad.finalproject.core.touch.Point;
import idoelad.finalproject.core.touch.Touch;
import idoelad.finalproject.core.userparams.UserParamsHolder;

import java.util.ArrayList;

import android.content.Context;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.LinearLayout;

public class MyLinearLayout extends LinearLayout {
	private static final String LOG_TAG = "TremorTouchLauncher";

	private ArrayList<Touch> currTouches;
	private static boolean isSyntetic = false;
	private static boolean isAfterUp = false;
	private Circle guessCircle;
	private long firstEventTime;



	public MyLinearLayout(Context context) {
		super(context);
	}

	public MyLinearLayout(Context context, AttributeSet attrs) {
		super(context,attrs);
	}

	
    /////// CORE ///////
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent e) {
		Log.i(LOG_TAG,"TOUCH | "+getTouchType(e.getAction()));

		if (MotionEvent.ACTION_DOWN == e.getAction()) {
			if (!isSyntetic){
				currTouches = new ArrayList<Touch>();
				firstEventTime = e.getEventTime();
				currTouches.add(eventToTouch(e));
				return true;
			}

			else{
				Log.i(LOG_TAG,"^ SYNTETIC TOUCH (DOWN)");
				super.dispatchTouchEvent(e);
				return true;
			}
		}

		else if (MotionEvent.ACTION_UP == e.getAction()) {
			if (!isAfterUp){
				//Create touch (DOWN) according to user's params
				isSyntetic = true;
				guessCircle = createSyntheticTouch();
				
				//Add "UP" event
				isAfterUp = true;
				simulateAction((float)guessCircle.getCenter().getX(), (float)guessCircle.getCenter().getX(), MotionEvent.ACTION_UP); //FIXME casting
				return true;
			}

			else{
				isAfterUp = false;
				Log.i(LOG_TAG,"^ SYNTETIC TOUCH (UP)");
				super.dispatchTouchEvent(e);
				isSyntetic = false;
				return true;
			}
		}

		else{
			currTouches.add(eventToTouch(e));
			return true;
		}
	}


	private Circle createSyntheticTouch(){
		int pointerId = MultiTouch.guessFingure(currTouches, UserParamsHolder.upMulti);
		ArrayList<Touch> filteredTouches = MultiTouch.filterTouchesByFinger(currTouches, pointerId);
		Circle circle = BigTouch.guessCircleBigTouch(filteredTouches, UserParamsHolder.upBig);
		double[] newLocation = DeviationTouch.getNewLocation(circle.getCenter().getX(), circle.getCenter().getY(), UserParamsHolder.upDev);
		circle.setCenter(new Point(newLocation[0], newLocation[1]));
		simulateAction((float)circle.getCenter().getX(),(float)circle.getCenter().getY(), MotionEvent.ACTION_DOWN); //FIXME casting
		return circle;
	}

	private void simulateAction(float x, float y, int action){
		MotionEvent newEvent = MotionEvent.obtain( 
				SystemClock.uptimeMillis(),
				SystemClock.uptimeMillis(), 
				action, x, y, 0);
		isSyntetic = true;
		dispatchTouchEvent(newEvent);
	}


	private Touch eventToTouch(MotionEvent e){
		String action = getTouchType(e.getAction());

		int count = e.getPointerCount();

		// get pointer ID
		int pointerIndex = e.getActionIndex();
		int pointerId = e.getPointerId(pointerIndex)+1; //TODO test 0/1
		return new Touch(new Point(e.getX(), e.getY()), action, e.getSize(),
				e.getPressure(), (e.getEventTime() - firstEventTime), count, pointerId); 
	}

	private String getTouchType(int action){
		switch (action){
		case MotionEvent.ACTION_DOWN:
			return "DOWN";
		case MotionEvent.ACTION_MOVE:
			return "MOVE";
		case MotionEvent.ACTION_UP:
			return "UP";
		default:
			return "UNKNOWN";
		}
	}

}
