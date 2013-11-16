package com.readystatesoftware.systemuitint.sample;

import android.app.Activity;
import android.os.Bundle;

import com.readystatesoftware.systemuitint.SystemUiTintManager;

public class ColorActivity extends Activity { 

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color);
  
		SystemUiTintManager tintManager = new SystemUiTintManager(this);
		tintManager.setStatusBarTintEnabled(true);
		tintManager.setNavigationBarTintEnabled(true);
    }
    
}
