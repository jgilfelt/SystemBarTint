/*
 * Copyright (C) 2013 readyState Software Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.readystatesoftware.systembartint.sample;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.chiralcode.colorpicker.ColorPicker;
import com.readystatesoftware.systembartint.SystemBarTintManager;

public class ColorActivity extends Activity { 

	private SystemBarTintManager mTintManager;
	private ColorPicker mColorPicker;
	private Button mButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_color);

		mTintManager = new SystemBarTintManager(this);
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
