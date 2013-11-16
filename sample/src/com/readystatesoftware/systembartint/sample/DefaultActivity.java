package com.readystatesoftware.systembartint.sample;

import android.app.Activity;
import android.os.Bundle;

import com.readystatesoftware.systembartint.SystemBarTintManager;

public class DefaultActivity extends Activity { 

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_default);
  
		SystemBarTintManager tintManager = new SystemBarTintManager(this);
		tintManager.setStatusBarTintEnabled(true);
		tintManager.setNavigationBarTintEnabled(true);
    }
    
}
