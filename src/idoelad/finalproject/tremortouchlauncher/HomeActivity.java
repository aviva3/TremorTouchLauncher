package idoelad.finalproject.tremortouchlauncher;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class HomeActivity extends Activity{
	private PackageManager manager;
	private List<AppDetail> apps; 

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		hideBars();
		setContentView(R.layout.activity_home);

		loadApps();
//		Button testButton = (Button) findViewById(R.id.testButton);
//		testButton.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				Log.i("@@", "BUTTON");	
//			}
//		});
	}

	@Override
	protected void onResume() {
		hideBars();
		super.onResume();
	}



	private void hideBars(){
		View decorView = getWindow().getDecorView();
		decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
				| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
				| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
				| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
				| View.SYSTEM_UI_FLAG_FULLSCREEN
				| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
	}


	public class AppDetail {
		CharSequence label;
		CharSequence name;
		Drawable icon;
	}
	
	private void loadApps(){
	    manager = getPackageManager();
	    apps = new ArrayList<AppDetail>();
	     
	    Intent i = new Intent(Intent.ACTION_MAIN, null);
	    i.addCategory(Intent.CATEGORY_LAUNCHER);
	     
	    List<ResolveInfo> availableActivities = manager.queryIntentActivities(i, 0);
	    for(ResolveInfo ri:availableActivities){
	        AppDetail app = new AppDetail();
	        app.label = ri.loadLabel(manager);
	        app.name = ri.activityInfo.packageName;
	        app.icon = ri.activityInfo.loadIcon(manager);
	        apps.add(app);
	    }
	}
}

