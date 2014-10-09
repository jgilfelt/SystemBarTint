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

import java.lang.reflect.Method;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout.LayoutParams;

/**
 * Class to manage status and navigation bar tint effects when using KitKat translucent system UI
 * modes.</p>
 */
public class SysBarTintManager {

    // ===========================================================
    // Constants
    // ===========================================================

    /**
     * The default system bar tint color value.
     * 
     * 60% opacity, black
     */
    public static final int DEFAULT_TINT_COLOR = 0x99000000;

    // ===========================================================
    // Static Fields
    // ===========================================================

    private static String sNavBarOverride;

    // ===========================================================
    // Static Initializers
    // ===========================================================

    static {
        // Android allows a system property to override the presence of the navigation bar.
        // Used by the emulator.
        // See
        // https://github.com/android/platform_frameworks_base/blob/master/policy/src/com/android/internal/policy/impl/PhoneWindowManager.java#L1076
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                final Class<?> sp = Class.forName("android.os.SystemProperties");
                final Method get = sp.getMethod("get", String.class);
                get.setAccessible(true);
                sNavBarOverride = (String) get.invoke(null, "qemu.hw.mainkeys");
            } catch (Throwable e) {
                sNavBarOverride = null;
            }
        }
    }

    // ===========================================================
    // Static Methods
    // ===========================================================

    // ===========================================================
    // Fields
    // ===========================================================

    private final SystemBarConfig mConfig;

    private boolean mStatusBarAvailable;

    private boolean mNavBarAvailable;

    private boolean mActionBarAvailable;

    private boolean mStatusBarTintEnabled;

    private boolean mNavBarTintEnabled;

    private boolean mActionBarTintEnabled;

    private View mStatusBarTintView;

    private View mNavBarTintView;

    private Drawable mOldActionBarBackground;

    private ActionBar mActionBar;

    // ===========================================================
    // Initializers
    // ===========================================================

    private final Drawable.Callback mDrawableCallback = new Drawable.Callback() {
        @Override
        public void invalidateDrawable(Drawable who) {
            mActionBar.setBackgroundDrawable(who);
        }

        @Override
        public void scheduleDrawable(Drawable who, Runnable what, long when) {
            new Handler().postAtTime(what, when);
        }

        @Override
        public void unscheduleDrawable(Drawable who, Runnable what) {
            new Handler().removeCallbacks(what);
        }
    };

    // ===========================================================
    // Constructors
    // ===========================================================

    /**
     * Constructor. Call this in the host activity onCreate method after its content view has been
     * set. You should always create new instances when the host activity is recreated.
     * 
     * @param activity
     *            The host activity.
     */
    @TargetApi(19)
    public SysBarTintManager(Activity activity) {
        final Window win = activity.getWindow();
        final ViewGroup decorViewGroup = (ViewGroup) win.getDecorView();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // check theme attrs
            final int[] attrs = {
                    android.R.attr.windowTranslucentStatus,
                    android.R.attr.windowTranslucentNavigation
            };
            final TypedArray a = activity.obtainStyledAttributes(attrs);
            try {
                mStatusBarAvailable = a.getBoolean(0, false);
                mNavBarAvailable = a.getBoolean(1, false);
            } finally {
                a.recycle();
            }

            // check window flags
            final WindowManager.LayoutParams winParams = win.getAttributes();
            int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
            if ((winParams.flags & bits) != 0) {
                mStatusBarAvailable = true;
            }
            bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION;
            if ((winParams.flags & bits) != 0) {
                mNavBarAvailable = true;
            }
        }

        mConfig = new SystemBarConfig(activity, mStatusBarAvailable, mNavBarAvailable);
        // device might not have virtual navigation keys
        if (!mConfig.hasNavigtionBar()) {
            mNavBarAvailable = false;
        }

        mActionBar = activity.getActionBar();
        mActionBarAvailable = mActionBar != null;

        if (mStatusBarAvailable) {
            setupStatusBarView(activity, decorViewGroup);
        }
        if (mNavBarAvailable) {
            setupNavBarView(activity, decorViewGroup);
        }
    }

    // ===========================================================
    // Getter & Setter
    // ===========================================================

    /**
     * Get the system bar configuration.
     * 
     * @return The system bar configuration for the current device configuration.
     */
    public SystemBarConfig getConfig() {
        return mConfig;
    }

    /**
     * Enable tinting of the system status bar.
     * 
     * If the platform is running Jelly Bean or earlier, or translucent system UI modes have not
     * been enabled in either the theme or via window flags, then this method does nothing.
     * 
     * @param enabled
     *            True to enable tinting, false to disable it (default).
     */
    public void setStatusBarTintEnabled(boolean enabled) {
        mStatusBarTintEnabled = enabled;
        if (mStatusBarAvailable) {
            mStatusBarTintView.setVisibility(enabled ? View.VISIBLE : View.GONE);
        }
    }

    /**
     * Enable tinting of the system navigation bar.
     * 
     * If the platform does not have soft navigation keys, is running Jelly Bean or earlier, or
     * translucent system UI modes have not been enabled in either the theme or via window flags,
     * then this method does nothing.
     * 
     * @param enabled
     *            True to enable tinting, false to disable it (default).
     */
    public void setNavigationBarTintEnabled(boolean enabled) {
        mNavBarTintEnabled = enabled;
        if (mNavBarAvailable) {
            mNavBarTintView.setVisibility(enabled ? View.VISIBLE : View.GONE);
        }
    }

    /**
     * Enable coloring of the {@link ActionBar}
     * 
     * @param enabled
     *            {@code true} to enable coloring the {@link ActionBar}
     */
    public void setActionBarTintEnabled(boolean enabled) {
        mActionBarTintEnabled = enabled;
    }

    // ===========================================================
    // Methods for/from SuperClass/Interfaces
    // ===========================================================

    // ===========================================================
    // Methods
    // ===========================================================

    /**
     * Apply the specified color tint to all system UI bars.
     * 
     * @param color
     *            The color of the background tint.
     */
    public void setTintColor(int color) {
        setStatusBarTintColor(color);
        setNavigationBarTintColor(color);
        setActionBarColor(color);
    }

    /**
     * Apply the specified drawable or color resource to all system UI bars.
     * 
     * @param res
     *            The identifier of the resource.
     */
    public void setTintResource(int res) {
        setStatusBarTintResource(res);
        setNavigationBarTintResource(res);
    }

    /**
     * Apply the specified drawable to all system UI bars.
     * 
     * @param drawable
     *            The drawable to use as the background, or null to remove it.
     */
    public void setTintDrawable(Drawable drawable) {
        setStatusBarTintDrawable(drawable);
        setNavigationBarTintDrawable(drawable);
    }

    /**
     * Apply the specified alpha to all system UI bars.
     * 
     * @param alpha
     *            The alpha to use
     */
    public void setTintAlpha(float alpha) {
        setStatusBarAlpha(alpha);
        setNavigationBarAlpha(alpha);
    }

    /**
     * Apply the specified color tint to the system status bar.
     * 
     * @param color
     *            The color of the background tint.
     */
    public void setStatusBarTintColor(int color) {
        if (mStatusBarAvailable) {
            mStatusBarTintView.setBackgroundColor(color);
        }
    }

    /**
     * Apply the specified drawable or color resource to the system status bar.
     * 
     * @param res
     *            The identifier of the resource.
     */
    public void setStatusBarTintResource(int res) {
        if (mStatusBarAvailable) {
            mStatusBarTintView.setBackgroundResource(res);
        }
    }

    /**
     * Apply the specified drawable to the system status bar.
     * 
     * @param drawable
     *            The drawable to use as the background, or null to remove it.
     */
    @SuppressWarnings("deprecation")
    public void setStatusBarTintDrawable(Drawable drawable) {
        if (mStatusBarAvailable) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                mStatusBarTintView.setBackground(drawable);
            } else {
                mStatusBarTintView.setBackgroundDrawable(drawable);
            }
        }
    }

    /**
     * Apply the specified alpha to the system status bar.
     * 
     * @param alpha
     *            The alpha to use
     */
    @TargetApi(11)
    public void setStatusBarAlpha(float alpha) {
        if (mStatusBarAvailable && Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mStatusBarTintView.setAlpha(alpha);
        }
    }

    /**
     * Apply the specified color tint to the system navigation bar.
     * 
     * @param color
     *            The color of the background tint.
     */
    public void setNavigationBarTintColor(int color) {
        if (mNavBarAvailable) {
            mNavBarTintView.setBackgroundColor(color);
        }
    }

    /**
     * Apply the specified drawable or color resource to the system navigation bar.
     * 
     * @param res
     *            The identifier of the resource.
     */
    public void setNavigationBarTintResource(int res) {
        if (mNavBarAvailable) {
            mNavBarTintView.setBackgroundResource(res);
        }
    }

    /**
     * Apply the specified drawable to the system navigation bar.
     * 
     * @param drawable
     *            The drawable to use as the background, or null to remove it.
     */
    @SuppressWarnings("deprecation")
    public void setNavigationBarTintDrawable(Drawable drawable) {
        if (mNavBarAvailable) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                mNavBarTintView.setBackground(drawable);
            } else {
                mNavBarTintView.setBackgroundDrawable(drawable);
            }
        }
    }

    /**
     * Apply the specified alpha to the system navigation bar.
     * 
     * @param alpha
     *            The alpha to use
     */
    @TargetApi(11)
    public void setNavigationBarAlpha(float alpha) {
        if (mNavBarAvailable && Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mNavBarTintView.setAlpha(alpha);
        }
    }

    public void setActionBarColor(int color) {
        if (mActionBarAvailable && mActionBarTintEnabled) {
            final Drawable colorDrawable = new ColorDrawable(color);

            if (mOldActionBarBackground == null) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    colorDrawable.setCallback(mDrawableCallback);
                } else {
                    mActionBar.setBackgroundDrawable(colorDrawable);
                }
            } else {
                final TransitionDrawable td = new TransitionDrawable(new Drawable[] {
                        mOldActionBarBackground, colorDrawable
                });

                // workaround for broken ActionBarContainer drawable handling on
                // pre-API 17 builds
                // https://github.com/android/platform_frameworks_base/commit/a7cc06d82e45918c37429a59b14545c6a57db4e4
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    td.setCallback(mDrawableCallback);
                } else {
                    mActionBar.setBackgroundDrawable(td);
                }

                td.startTransition(200);
            }

            mOldActionBarBackground = colorDrawable;

            // http://stackoverflow.com/questions/11002691/actionbar-setbackgrounddrawable-nulling-background-from-thread-handler
            boolean isDisplayingTitle = (mActionBar.getDisplayOptions() & ActionBar.DISPLAY_SHOW_TITLE) != 0;
            mActionBar.setDisplayShowTitleEnabled(!isDisplayingTitle);
            mActionBar.setDisplayShowTitleEnabled(isDisplayingTitle);
        }
    }

    /**
     * Is tinting enabled for the system status bar?
     * 
     * @return True if enabled, False otherwise.
     */
    public boolean isStatusBarTintEnabled() {
        return mStatusBarTintEnabled;
    }

    /**
     * Is tinting enabled for the system navigation bar?
     * 
     * @return True if enabled, False otherwise.
     */
    public boolean isNavBarTintEnabled() {
        return mNavBarTintEnabled;
    }

    /**
     * Is tinting the {@link ActionBar} enabled?
     * 
     * @return True if enabled, False otherwise
     */
    public boolean isActionBarTintEnabled() {
        return mActionBarTintEnabled;
    }

    private void setupStatusBarView(Context context, ViewGroup decorViewGroup) {
        mStatusBarTintView = new View(context);
        final LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
                mConfig.getStatusBarHeight());
        params.gravity = Gravity.TOP;
        if (mNavBarAvailable && !mConfig.isNavigationAtBottom()) {
            params.rightMargin = mConfig.getNavigationBarWidth();
        }
        mStatusBarTintView.setLayoutParams(params);
        mStatusBarTintView.setBackgroundColor(DEFAULT_TINT_COLOR);
        mStatusBarTintView.setVisibility(View.GONE);
        decorViewGroup.addView(mStatusBarTintView);
    }

    private void setupNavBarView(Context context, ViewGroup decorViewGroup) {
        mNavBarTintView = new View(context);
        final LayoutParams params;
        if (mConfig.isNavigationAtBottom()) {
            params = new LayoutParams(LayoutParams.MATCH_PARENT, mConfig.getNavigationBarHeight());
            params.gravity = Gravity.BOTTOM;
        } else {
            params = new LayoutParams(mConfig.getNavigationBarWidth(), LayoutParams.MATCH_PARENT);
            params.gravity = Gravity.RIGHT;
        }
        mNavBarTintView.setLayoutParams(params);
        mNavBarTintView.setBackgroundColor(DEFAULT_TINT_COLOR);
        mNavBarTintView.setVisibility(View.GONE);
        decorViewGroup.addView(mNavBarTintView);
    }

    // ===========================================================
    // Inner and Anonymous Classes and/or Interfaces
    // ===========================================================

    /**
     * Class which describes system bar sizing and other characteristics for the current device
     * configuration.
     * 
     */
    public static class SystemBarConfig {

        private static final String STATUS_BAR_HEIGHT_RES_NAME = "status_bar_height";

        private static final String NAV_BAR_HEIGHT_RES_NAME = "navigation_bar_height";

        private static final String NAV_BAR_HEIGHT_LANDSCAPE_RES_NAME = "navigation_bar_height_landscape";

        private static final String NAV_BAR_WIDTH_RES_NAME = "navigation_bar_width";

        private static final String SHOW_NAV_BAR_RES_NAME = "config_showNavigationBar";

        private final boolean mTranslucentStatusBar;

        private final boolean mTranslucentNavBar;

        private final int mStatusBarHeight;

        private final int mActionBarHeight;

        private final boolean mHasNavigationBar;

        private final int mNavigationBarHeight;

        private final int mNavigationBarWidth;

        private final boolean mInPortrait;

        private final float mSmallestWidthDp;

        private SystemBarConfig(Activity activity, boolean translucentStatusBar,
                boolean traslucentNavBar) {
            final Resources res = activity.getResources();
            mInPortrait = (res.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT);
            mSmallestWidthDp = getSmallestWidthDp(activity);
            mStatusBarHeight = getInternalDimensionSize(res, STATUS_BAR_HEIGHT_RES_NAME);
            mActionBarHeight = getActionBarHeight(activity);
            mNavigationBarHeight = getNavigationBarHeight(activity);
            mNavigationBarWidth = getNavigationBarWidth(activity);
            mHasNavigationBar = (mNavigationBarHeight > 0);
            mTranslucentStatusBar = translucentStatusBar;
            mTranslucentNavBar = traslucentNavBar;
        }

        @TargetApi(14)
        private int getActionBarHeight(Context context) {
            int result = 0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                final TypedValue tv = new TypedValue();
                context.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true);
                result = TypedValue.complexToDimensionPixelSize(tv.data, context.getResources()
                        .getDisplayMetrics());
            }
            return result;
        }

        @TargetApi(14)
        private int getNavigationBarHeight(Context context) {
            final Resources res = context.getResources();
            int result = 0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                if (hasNavBar(context)) {
                    final String key;
                    if (mInPortrait) {
                        key = NAV_BAR_HEIGHT_RES_NAME;
                    } else {
                        key = NAV_BAR_HEIGHT_LANDSCAPE_RES_NAME;
                    }
                    return getInternalDimensionSize(res, key);
                }
            }
            return result;
        }

        @TargetApi(14)
        private int getNavigationBarWidth(Context context) {
            final Resources res = context.getResources();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                if (hasNavBar(context)) {
                    return getInternalDimensionSize(res, NAV_BAR_WIDTH_RES_NAME);
                }
            }
            return 0;
        }

        @TargetApi(14)
        private boolean hasNavBar(Context context) {
            final Resources res = context.getResources();
            int resourceId = res.getIdentifier(SHOW_NAV_BAR_RES_NAME, "bool", "android");
            if (resourceId != 0) {
                boolean hasNav = res.getBoolean(resourceId);
                // check override flag (see static block)
                if ("1".equals(sNavBarOverride)) {
                    hasNav = false;
                } else if ("0".equals(sNavBarOverride)) {
                    hasNav = true;
                }
                return hasNav;
            } else { // fallback
                return !ViewConfiguration.get(context).hasPermanentMenuKey();
            }
        }

        private int getInternalDimensionSize(Resources res, String key) {
            int result = 0;
            final int resourceId = res.getIdentifier(key, "dimen", "android");
            if (resourceId > 0) {
                result = res.getDimensionPixelSize(resourceId);
            }
            return result;
        }

        @SuppressLint("NewApi")
        private float getSmallestWidthDp(Activity activity) {
            final DisplayMetrics metrics = new DisplayMetrics();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                activity.getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
            } else {
                // TODO this is not correct, but we don't really care pre-kitkat
                activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
            }
            final float widthDp = metrics.widthPixels / metrics.density;
            final float heightDp = metrics.heightPixels / metrics.density;
            return Math.min(widthDp, heightDp);
        }

        /**
         * Should a navigation bar appear at the bottom of the screen in the current device
         * configuration? A navigation bar may appear on the right side of the screen in certain
         * configurations.
         * 
         * @return True if navigation should appear at the bottom of the screen, False otherwise.
         */
        public boolean isNavigationAtBottom() {
            return (mSmallestWidthDp >= 600 || mInPortrait);
        }

        /**
         * Get the height of the system status bar.
         * 
         * @return The height of the status bar (in pixels).
         */
        public int getStatusBarHeight() {
            return mStatusBarHeight;
        }

        /**
         * Get the height of the action bar.
         * 
         * @return The height of the action bar (in pixels).
         */
        public int getActionBarHeight() {
            return mActionBarHeight;
        }

        /**
         * Does this device have a system navigation bar?
         * 
         * @return True if this device uses soft key navigation, False otherwise.
         */
        public boolean hasNavigtionBar() {
            return mHasNavigationBar;
        }

        /**
         * Get the height of the system navigation bar.
         * 
         * @return The height of the navigation bar (in pixels). If the device does not have soft
         *         navigation keys, this will always return 0.
         */
        public int getNavigationBarHeight() {
            return mNavigationBarHeight;
        }

        /**
         * Get the width of the system navigation bar when it is placed vertically on the screen.
         * 
         * @return The width of the navigation bar (in pixels). If the device does not have soft
         *         navigation keys, this will always return 0.
         */
        public int getNavigationBarWidth() {
            return mNavigationBarWidth;
        }

        /**
         * Get the layout inset for any system UI that appears at the top of the screen.
         * 
         * @param withActionBar
         *            True to include the height of the action bar, False otherwise.
         * @return The layout inset (in pixels).
         */
        public int getPixelInsetTop(boolean withActionBar) {
            return (mTranslucentStatusBar ? mStatusBarHeight : 0)
                    + (withActionBar ? mActionBarHeight : 0);
        }

        /**
         * Get the layout inset for any system UI that appears at the bottom of the screen.
         * 
         * @return The layout inset (in pixels).
         */
        public int getPixelInsetBottom() {
            if (mTranslucentNavBar && isNavigationAtBottom()) {
                return mNavigationBarHeight;
            } else {
                return 0;
            }
        }

        /**
         * Get the layout inset for any system UI that appears at the right of the screen.
         * 
         * @return The layout inset (in pixels).
         */
        public int getPixelInsetRight() {
            if (mTranslucentNavBar && !isNavigationAtBottom()) {
                return mNavigationBarWidth;
            } else {
                return 0;
            }
        }

    }
}
