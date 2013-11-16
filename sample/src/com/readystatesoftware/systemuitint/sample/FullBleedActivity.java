package com.readystatesoftware.systemuitint.sample;

import com.readystatesoftware.systemuitint.SystemUiTintManager;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

public class FullBleedActivity extends Activity { 

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_bleed);
        
        //setTranslucentStatus(true);
		//setTranslucentNavigation(true);

		SystemUiTintManager tintManager = new SystemUiTintManager(this);
		tintManager.setStatusBarTintEnabled(true);
		tintManager.setNavigationBarTintEnabled(true);
		//tintManager.setStatusBarTintColor(Color.parseColor("#99ff0000"));
		//tintManager.setNavigationBarTintResource(android.R.drawable.bottom_bar);
		
    }
    
    @TargetApi(19) 
    private void setTranslucentStatus(boolean on) {
		Window win = getWindow();
		WindowManager.LayoutParams winParams = win.getAttributes();
		final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
		if (on) {
			winParams.flags |= bits;
		} else {
			winParams.flags &= ~bits;
		}
		win.setAttributes(winParams);
	}

	@TargetApi(19) 
	private void setTranslucentNavigation(boolean on) {
		Window win = getWindow();
		WindowManager.LayoutParams winParams = win.getAttributes();
		final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION;
		if (on) {
			winParams.flags |= bits;
		} else {
			winParams.flags &= ~bits;
		}
		win.setAttributes(winParams);
	}
    
}
