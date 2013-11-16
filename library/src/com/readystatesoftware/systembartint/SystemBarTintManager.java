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

package com.readystatesoftware.systembartint;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout.LayoutParams;

public class SystemBarTintManager {

	public static final int DEFAULT_TINT_COLOR = Color.parseColor("#99000000");

	private SystemBarConfig mConfig;
	private boolean mStatusBarAvailable;
	private boolean mNavBarAvailable;
	private boolean mStatusBarTintEnabled;
	private boolean mNavBarTintEnabled;
	private View mStatusBarTintView;
	private View mNavBarTintView;

	@TargetApi(19) 
	public SystemBarTintManager(Activity activity) {

		mConfig = new SystemBarConfig(activity);
		Window win = activity.getWindow();
		ViewGroup decorViewGroup = (ViewGroup) win.getDecorView();

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			// check theme attrs
			int[] attrs = {android.R.attr.windowTranslucentStatus,
					android.R.attr.windowTranslucentNavigation};
			TypedArray a = activity.obtainStyledAttributes(attrs);
			mStatusBarAvailable = a.getBoolean(0, false);
			mNavBarAvailable = a.getBoolean(1, false);

			// check window flags
			WindowManager.LayoutParams winParams = win.getAttributes();
			int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
			if ((winParams.flags & bits) != 0) {
				mStatusBarAvailable = true;
			}
			bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION;
			if ((winParams.flags & bits) != 0) {
				mNavBarAvailable = true;
			}

			// device might not have virtual navigation keys
			if (!mConfig.hasNavigtionBar()) {
				mNavBarAvailable = false;
			}
		}
		if (mStatusBarAvailable) {
			setupStatusBarView(activity, decorViewGroup);
		}
		if (mNavBarAvailable) {
			setupNavBarView(activity, decorViewGroup);
		}

	}

	public void setStatusBarTintEnabled(boolean enabled) {
		mStatusBarTintEnabled = enabled;
		if (mStatusBarAvailable) {
			mStatusBarTintView.setVisibility(enabled ? View.VISIBLE : View.GONE);
		}
	}

	public void setNavigationBarTintEnabled(boolean enabled) {
		mNavBarTintEnabled = enabled;
		if (mNavBarAvailable) {
			mNavBarTintView.setVisibility(enabled ? View.VISIBLE : View.GONE);
		}
	}

	public void setTintColor(int color) {
		setStatusBarTintColor(color);
		setNavigationBarTintColor(color);
	}

	public void setTintResource(int res) {
		setStatusBarTintResource(res);
		setNavigationBarTintResource(res);
	}

	public void setTintDrawable(Drawable drawable) {
		setStatusBarTintDrawable(drawable);
		setNavigationBarTintDrawable(drawable);
	}

	public void setStatusBarTintColor(int color) {
		if (mStatusBarAvailable) {
			mStatusBarTintView.setBackgroundColor(color);
		}
	}

	public void setStatusBarTintResource(int res) {
		if (mStatusBarAvailable) {
			mStatusBarTintView.setBackgroundResource(res);
		}
	}

	@SuppressWarnings("deprecation")
	public void setStatusBarTintDrawable(Drawable drawable) {
		if (mStatusBarAvailable) {
			mStatusBarTintView.setBackgroundDrawable(drawable);
		}
	}

	public void setNavigationBarTintColor(int color) {
		if (mNavBarAvailable) {
			mNavBarTintView.setBackgroundColor(color);
		}
	}

	public void setNavigationBarTintResource(int res) {
		if (mNavBarAvailable) {
			mNavBarTintView.setBackgroundResource(res);
		}
	}

	@SuppressWarnings("deprecation")
	public void setNavigationBarTintDrawable(Drawable drawable) {
		if (mNavBarAvailable) {
			mNavBarTintView.setBackgroundDrawable(drawable);
		}
	}

	public SystemBarConfig getConfig() {
		return mConfig;
	}

	public boolean isStatusBarTintEnabled() {
		return mStatusBarTintEnabled;
	}

	public boolean isNavBarTintEnabled() {
		return mNavBarTintEnabled;
	}

	private void setupStatusBarView(Context context, ViewGroup decorViewGroup) {
		mStatusBarTintView = new View(context);
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, mConfig.getStatusBarHeight());
		params.gravity = Gravity.TOP;
		mStatusBarTintView.setLayoutParams(params);
		mStatusBarTintView.setBackgroundColor(DEFAULT_TINT_COLOR);
		mStatusBarTintView.setVisibility(View.GONE);
		decorViewGroup.addView(mStatusBarTintView);
	}

	private void setupNavBarView(Context context, ViewGroup decorViewGroup) {
		mNavBarTintView = new View(context);
		LayoutParams params;
		if (mConfig.isNavigationAtBottom()) {
			params = new LayoutParams(LayoutParams.MATCH_PARENT, mConfig.getNavigationBarHeight());
			params.gravity = Gravity.BOTTOM;
		} else {
			params = new LayoutParams(mConfig.getNavigationBarHeight(), LayoutParams.MATCH_PARENT);
			params.gravity = Gravity.RIGHT;
			params.topMargin = mConfig.getStatusBarHeight();
		}
		mNavBarTintView.setLayoutParams(params);
		mNavBarTintView.setBackgroundColor(DEFAULT_TINT_COLOR);
		mNavBarTintView.setVisibility(View.GONE);
		decorViewGroup.addView(mNavBarTintView);
	}

	public class SystemBarConfig {

		private static final String STATUS_BAR_HEIGHT_RES_NAME = "status_bar_height";
		private static final String NAV_BAR_HEIGHT_RES_NAME = "navigation_bar_height";
		private static final String NAV_BAR_HEIGHT_LANDSCAPE_RES_NAME = "navigation_bar_height_landscape";

		private int mStatusBarHeight;
		private int mActionBarHeight;
		private boolean mHasNavigationBar;
		private int mNavigationBarHeight;
		private boolean mInPortrait;
		private float mSmallestWidthDp;

		private SystemBarConfig(Activity activity) {
			mInPortrait = (activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT);
			mSmallestWidthDp = getSmallestWidthDp(activity);
			mStatusBarHeight = getStatusBarHeight(activity);
			mActionBarHeight = getActionBarHeight(activity);
			mNavigationBarHeight = getNavigationBarHeight(activity);
			mHasNavigationBar = (mNavigationBarHeight > 0);
		}

		@TargetApi(14) 
		private int getActionBarHeight(Context context) {
			int result = 0;
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
				TypedValue tv = new TypedValue();
				context.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true);
				result = context.getResources().getDimensionPixelSize(tv.resourceId);
			}
			return result;
		}

		private int getStatusBarHeight(Context context) {
			Resources r = context.getResources();
			int result = 0;
			int resourceId = r.getIdentifier(STATUS_BAR_HEIGHT_RES_NAME, "dimen", "android");
			if (resourceId > 0) {
				result = r.getDimensionPixelSize(resourceId);
			}
			return result;
		}

		@TargetApi(14) 
		private int getNavigationBarHeight(Context context) {
			Resources r = context.getResources();
			int result = 0;
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
				if (!ViewConfiguration.get(context).hasPermanentMenuKey()) {
					String key;
					if (mInPortrait) {
						key = NAV_BAR_HEIGHT_RES_NAME;
					} else {
						key = NAV_BAR_HEIGHT_LANDSCAPE_RES_NAME;
					}
					int resourceId = r.getIdentifier(key, "dimen", "android");
					if (resourceId > 0) {
						result = r.getDimensionPixelSize(resourceId);
					}
				}
			}
			return result;
		}

		@SuppressLint("NewApi")
		private float getSmallestWidthDp(Activity activity) {
			DisplayMetrics metrics = new DisplayMetrics();
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
				activity.getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
			} else {
				// TODO this is not correct but we don't really care pre-kitkat
				activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
			}
			float widthDp = metrics.widthPixels / metrics.density;
			float heightDp = metrics.heightPixels / metrics.density;
			return Math.min(widthDp, heightDp);
		}

		public boolean isNavigationAtBottom() {
			return (mSmallestWidthDp > 600 || mInPortrait);
		}

		public int getStatusBarHeight() {
			return mStatusBarHeight;
		}

		public int getActionBarHeight() {
			return mActionBarHeight;
		}

		public boolean hasNavigtionBar() {
			return mHasNavigationBar;
		}

		public int getNavigationBarHeight() {
			return mNavigationBarHeight;
		}

		public int getPixelOffsetTop(boolean withActionBar) {
			return mStatusBarHeight + (withActionBar ? mActionBarHeight : 0);
		}

		public int getPixelOffestBottom() {
			if (isNavigationAtBottom()) {
				return mNavigationBarHeight;
			} else {
				return 0;
			}
		}

		public int getPixelOffsetRight() {
			if (!isNavigationAtBottom()) {
				return mNavigationBarHeight;
			} else {
				return 0;
			}
		}

	}

}
