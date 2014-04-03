Android System Bar Tint
=======================

Apply background tinting to the Android system UI when using KitKat translucent modes.  

![screenshot](https://raw.github.com/jgilfelt/SystemBarTint/master/screenshot.png "screenshot")

Android 4.4 (KitKat) introduced translucent system UI styling for status and navigation bars. These styles are great for wallpaper based activities like the home screen launcher, but the minimal background protection provided makes them less useful for other types of activity unless you supply your own backgrounds inside your layout. Determining the size, position and existence of the system UI for a given device configuration can be non-trivial.

This library offers a simple way to create a background "tint" for the system bars, be it a color value or Drawable. By default it will give you a semi-opaque black background that will be useful for full-bleed content screens where persistent system UI is still important - like when placed over a map or photo grid.

Usage
-----

You must first enable translucency in your Activity - either by using or inheriting from one of the various `*.TranslucentDecor` themes, by setting the `android:windowTranslucentNavigation` or `android:windowTranslucentStatus` theme attributes to `true` or by applying the `FLAG_TRANSLUCENT_NAVIGATION` or `FLAG_TRANSLUCENT_STATUS` flags to your Activity window in code.

If translucency is not enabled or your app is running on a platform version earlier than API 19, the system UI will appear as normal. You should not enable tinting when using fullscreen or immersive window modes. You can safely use this library on Android versions back to API 10.

To enable the tint:

```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    // create our manager instance after the content view is set
    SystemBarTintManager tintManager = new SystemBarTintManager(this);
    // enable status bar tint
    tintManager.setStatusBarTintEnabled(true);
    // enable navigation bar tint
    tintManager.setNavigationBarTintEnabled(true);
}
```

To provide custom tint color values or drawables:

```java
// set a custom tint color for all system bars
tintManager.setTintColor(Color.parseColor("#99000FF"));
// set a custom navigation bar resource
tintManager.setNavigationBarTintResource(R.drawable.my_tint);
// set a custom status bar drawable
tintManager.setStatusBarTintDrawable(MyDrawable);
```

#### SystemBarConfig

Developers should not need to concern themselves with the size or positioning of the system UI. Use `android:fitsSystemWindows="true"` in conjunction with `android:clipToPadding="false"` to achieve the optimal layout for full bleed content screens that need to be padded within the system UI bounds. However, certain elements like the `GoogleMap` provided by Google Play Services may force you to determine the pixel insets for the system bars in order to provide the appropriate layout effect.

Use the `SystemBarConfig` class provided by `SystemBarTintManager` to access those inset values:

```java
SystemBarConfig config = tintManager.getConfig();
map.setPadding(0, config.getPixelInsetTop(), config.getPixelInsetRight(), config.getPixelInsetBottom());
```

Setup
-----

Download and include the [JAR][1] in your project, or add the dependency in your `build.gradle`:

```groovy
dependencies {
    compile 'com.readystatesoftware.systembartint:systembartint:1.0.3'
}
```

This repository also includes a sample app which you can download from Google Play:

<a href="https://play.google.com/store/apps/details?id=com.readystatesoftware.systembartint.sample">
  <img alt="Android app on Google Play"
       src="https://developer.android.com/images/brand/en_app_rgb_wo_60.png" />
</a>

Credits
-------

Author: [Jeff Gilfelt](https://github.com/jgilfelt)

Sample app uses [Android-Color-Picker](https://github.com/chiralcode/Android-Color-Picker/) by [chiralcode](https://github.com/chiralcode)

License
-------

    Copyright 2013 readyState Software Limited

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

 [1]: http://repository.sonatype.org/service/local/artifact/maven/redirect?r=central-proxy&g=com.readystatesoftware.systembartint&a=systembartint&v=LATEST&&c=jar