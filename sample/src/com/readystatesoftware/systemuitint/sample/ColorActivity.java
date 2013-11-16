package com.readystatesoftware.systemuitint.sample;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.chiralcode.colorpicker.ColorPicker;
import com.readystatesoftware.systemuitint.SystemUiTintManager;

public class ColorActivity extends Activity { 

	private SystemUiTintManager mTintManager;
	private ColorPicker mColorPicker;
    private Button mButton;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color);
  
        mTintManager = new SystemUiTintManager(this);
        mTintManager.setStatusBarTintEnabled(true);
        mTintManager.setNavigationBarTintEnabled(true);
		
		mColorPicker = (ColorPicker) findViewById(R.id.color_picker);
		applySelectedColor();
		
		mButton = (Button) findViewById(R.id.button);
		mButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				applySelectedColor();
			}
        });
    }
    
    @Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		applySelectedColor();
	}

	private void applySelectedColor() {
    	int selected = mColorPicker.getColor();
		int color = Color.argb(153, Color.red(selected), Color.green(selected), Color.blue(selected));
		mTintManager.setTintColor(color);
    }
    
}
