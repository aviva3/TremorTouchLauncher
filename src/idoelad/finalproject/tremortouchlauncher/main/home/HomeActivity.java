package idoelad.finalproject.tremortouchlauncher.main.home;

import idoelad.finalproject.core.bigtouch.BigTouch;
import idoelad.finalproject.core.bigtouch.UserParamsBigTouch;
import idoelad.finalproject.core.deviationtouch.DeviationTouch;
import idoelad.finalproject.core.deviationtouch.TargertNGuess;
import idoelad.finalproject.core.deviationtouch.UserParamsDeviationTouch;
import idoelad.finalproject.core.multitouch.MultiTouch;
import idoelad.finalproject.core.multitouch.UserParamsMultiTouch;
import idoelad.finalproject.core.touch.Circle;
import idoelad.finalproject.core.touch.Point;
import idoelad.finalproject.core.touch.Test;
import idoelad.finalproject.core.touch.Touch;
import idoelad.finalproject.core.userparams.UserParamsHandler;
import idoelad.finalproject.core.userparams.UserParamsHolder;
import idoelad.finalproject.tremortouchlauncher.R;
import idoelad.finalproject.tremortouchlauncher.main.manual.ManualActivity;
import idoelad.finalproject.tremortouchlauncher.training.ChooseTestActivity;
import idoelad.finalproject.tremortouchlauncher.training.CirclesFilesHandler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.TextView;

public class HomeActivity extends Activity {

	private static final String LOG_TAG = "TremorTouchLauncher";
	public static final String LOG_TOUCH_FLOW = "Touch Flow";
	public static String APP_NAME;

	private static final HashSet<String> DEFAULT_DOCK_APPS = new HashSet<String>(
			Arrays.asList("Maps", "Chrome", "Facebook", "Messaging", "Settings"));
	private ArrayList<LaunchableAppInfo> mApplications;
	private ArrayList<LaunchableAppInfo> mDockedApplications;
	public static GridView gridView;
	
	protected float guessX;
	protected float guessY;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		String appName = getResources().getString(R.string.app_name);
		APP_NAME = appName;

		hideBars();
		setContentView(R.layout.activity_home);

		// Prevent from keyboard to open on activity start
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		Button settingsBtn = (Button) findViewById(R.id.btn_settings);
		settingsBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				/** Instantiating PopupMenu class */
				PopupMenu settingsMenu = new PopupMenu(HomeActivity.this, v);

				/** Adding menu items to the popumenu */
				settingsMenu.getMenuInflater().inflate(R.menu.settings_menu,
						settingsMenu.getMenu());

				/** Defining menu item click listener for the popup menu */
				settingsMenu
						.setOnMenuItemClickListener(new OnMenuItemClickListener() {

							@Override
							public boolean onMenuItemClick(MenuItem item) {
								CharSequence title = item.getTitle();
								if (title.equals(getResources().getString(
										R.string.settings_training))) {
									Intent startTestActivityIntent = new Intent(
											HomeActivity.this,
											ChooseTestActivity.class);
									startActivityForResult(
											startTestActivityIntent, 0);
								} else if (title.equals(getResources()
										.getString(R.string.settings_manual))) {
									Intent startManualActivityIntent = new Intent(
											HomeActivity.this,
											ManualActivity.class);
									startActivityForResult(
											startManualActivityIntent, 0);
								}
								return true;
							}
						});
				/** Showing the popup menu */
				settingsMenu.show();
			}
		});

		createResources();
		loadInitUserParams();

		loadApps();
		bindApps();
		bindDockedApps();
	}

	@Override
	protected void onResume() {
		hideBars();
		super.onResume();
	}
	
	private void createResources() {
		// Create app folder
		File extStore = Environment.getExternalStorageDirectory();
		File appFolder = new File(extStore, HomeActivity.APP_NAME);
		if (!appFolder.exists()) {
			appFolder.mkdir();
		}

		// Create userParams
		try {
			UserParamsHandler.initUserParamsFiles();
		} catch (IOException e) {
			Log.e(LOG_TAG, "Unable to init user params: " + e.getMessage());
		}

		// Create training folder
		File trainingFolder = new File(appFolder, "training");
		if (!trainingFolder.exists()) {
			trainingFolder.mkdir();
		}

		// Create circles
		try {
			CirclesFilesHandler.initCircles(trainingFolder);
		} catch (IOException e) {
			Log.e(LOG_TAG, "Unable to init circles: " + e.getMessage());
		}
	}

	private void loadInitUserParams() {
		try {
			UserParamsHolder.upBig = UserParamsHandler.loadUserParamsBig();
			UserParamsHolder.upMulti = UserParamsHandler.loadUserParamsMulti();
			UserParamsHolder.upDev = UserParamsHandler.loadUserParamsDev();
		} catch (IOException e) {
			Log.e(LOG_TAG,
					"Error while loading user parameters: " + e.getMessage());
			Toast.makeText(getApplicationContext(),
					"Error while loading user parameters", Toast.LENGTH_SHORT)
					.show();
			finish();
		}

	}

	private void loadApps() {
		PackageManager packManager = getPackageManager();

		Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

		final List<ResolveInfo> apps = packManager.queryIntentActivities(
				mainIntent, 0);
		Collections.sort(apps, new ResolveInfo.DisplayNameComparator(
				packManager));

		if (mApplications == null)
			mApplications = new ArrayList<LaunchableAppInfo>(apps.size());
		else
			mApplications.clear();

		if (mDockedApplications == null)
			mDockedApplications = new ArrayList<LaunchableAppInfo>();

		if (apps != null)
			for (ResolveInfo qinfo : apps) {
				LaunchableAppInfo info = new LaunchableAppInfo(
						new ComponentName(
								qinfo.activityInfo.applicationInfo.packageName,
								qinfo.activityInfo.name));
				info.label = qinfo.loadLabel(packManager);
				info.icon = qinfo.loadIcon(packManager);

				if (!mDockedApplications.contains(info)
						&& DEFAULT_DOCK_APPS.contains(info.label))
					mDockedApplications.add(info);
				else
					mApplications.add(info);

				Log.v(LOG_TAG, info.toString());
			}

	}

	private void bindApps() {
		
		gridView = (GridView) findViewById(R.id.grid_view);
		gridView.setAdapter(new ImageArrayAdapter(this, mApplications));
		
		gridView.setOnTouchListener(new OnTouchListener() {	
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getActionMasked() == MotionEvent.ACTION_DOWN){
					Log.i(LOG_TOUCH_FLOW,"Grid touch is at: "+event.getRawX()+","+event.getRawY());
					guessX = event.getRawX();
					guessY = event.getRawY();
				}
				return false;
			}
		});
		
		gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				
				//Find Item coordinates in grid
				int[] itemTopLeft = new int[2]; 
		        v.getLocationOnScreen(itemTopLeft);
		        int itemWidth = v.getMeasuredWidth();
		        int itemHeight = v.getMeasuredHeight();
		        int itemCenterX = itemTopLeft[0] + itemWidth/2;
		        int itemCenterY = itemTopLeft[1] + itemHeight/2;
		        Log.i(LOG_TOUCH_FLOW,"Item is at: "+itemCenterX+","+itemCenterY+" {["+itemTopLeft[0]+","+(itemTopLeft[0]+itemWidth)+"] X ["+itemTopLeft[1]+","+(itemTopLeft[1]+itemHeight)+"]}");
				
		        //Launch applictaion
		        LaunchableAppInfo info = (LaunchableAppInfo) parent
						.getItemAtPosition(position);
				startActivity(info.intent);
				
				//Adjust userParams according to fake touch and item location
				trainUserParams(itemCenterX, itemCenterY, itemWidth, itemHeight);
			}
		});

	}
	
	private void trainUserParams(int itemCenterX, int itemCenterY, int itemWidth, int itemHeight){
		Log.i(LOG_TOUCH_FLOW,"Training. item: ("+itemCenterX+","+itemCenterY+") | guess: ("+guessX+","+guessY+")");
		int radius = itemWidth < itemHeight ? itemWidth/2 : itemHeight/2;
		Circle target = new Circle(new Point(itemCenterX, itemCenterY), radius);
		ArrayList<Touch> touches = new ArrayList<Touch>(MyLinearLayout.currTouches);
		updateUserParams(touches, target);
	}
	
	private void updateUserParams(ArrayList<Touch> touches, Circle target){
		ArrayList<Test> tests = new ArrayList<Test>();
		Test t = new Test(target);
		t.setTouches(touches);
		tests.add(t);
		UserParamsBigTouch upBig = BigTouch.getBigTouchParams(tests);
		UserParamsMultiTouch upMulti = MultiTouch.getMultiTouchParams(tests);
		
		ArrayList<TargertNGuess> tngs = new ArrayList<TargertNGuess>();
		tngs.add(new TargertNGuess(target, new Circle(new Point(guessX, guessY), 30)));
		UserParamsDeviationTouch upDev = DeviationTouch.getDeviationTouchParams(tngs);	
		try {
			UserParamsHandler.updateUserParamsFromTraining(upBig, upMulti, upDev, 0.01);
		} catch (IOException e) {
			Toast.makeText(getApplicationContext(), "Cannot update user parameters on live training.", Toast.LENGTH_SHORT).show();
		}
	}

	private void bindDockedApps() {

		GridView dockView = (GridView) findViewById(R.id.dock_view);
		dockView.setAdapter(new ImageArrayAdapter(this, mDockedApplications));

		dockView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				LaunchableAppInfo info = (LaunchableAppInfo) parent
						.getItemAtPosition(position);
				startActivity(info.intent);
			}
		});

	}

	private void hideBars() {
		View decorView = getWindow().getDecorView();
		decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
				| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
				| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
				| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
				| View.SYSTEM_UI_FLAG_FULLSCREEN
				| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
	}

	private class ImageArrayAdapter extends ArrayAdapter<LaunchableAppInfo> {

		public ImageArrayAdapter(Context context,
				ArrayList<LaunchableAppInfo> apps) {
			super(context, 0, apps);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final LaunchableAppInfo app = getItem(position);
			Log.d(LOG_TAG, "Position " + position + ": " + app);

			convertView = getLayoutInflater()
					.inflate(R.layout.app_button, null);
			ImageView appIcon = (ImageView) convertView
					.findViewById(R.id.app_icon);
			appIcon.setImageDrawable(app.icon);

			TextView appLabel = (TextView) convertView
					.findViewById(R.id.app_label);
			appLabel.setTextColor(Color.WHITE);
			appLabel.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
			appLabel.setText(app.label);

			return convertView;
		}

	}
}
