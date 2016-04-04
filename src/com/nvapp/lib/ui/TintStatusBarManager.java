/**
 * NV-APP
 */
package com.nvapp.lib.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Build;
import android.view.Window;
import android.view.WindowManager;

/**
 * Tint Status Bar
 */
public class TintStatusBarManager {
	/**
	 * 根据activity, color改变状态栏背景
	 * 
	 * @param activity activity
	 * @param resourceColor 颜色
	 */
	@SuppressLint({ "InlinedApi", "NewApi" })
	public static void tintByActivity(Activity activity, int resourceColor) {
		Window wnd = activity.getWindow();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			WindowManager.LayoutParams winParams = wnd.getAttributes();
			winParams.flags = winParams.flags | WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS;
			wnd.setAttributes(winParams);

			wnd.setStatusBarColor(activity.getResources().getColor(resourceColor));
		} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			WindowManager.LayoutParams winParams = wnd.getAttributes();
			winParams.flags = winParams.flags | WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
			wnd.setAttributes(winParams);

			SystemBarTintManager tintManager = new SystemBarTintManager(activity);
			tintManager.setStatusBarTintEnabled(true);
			tintManager.setStatusBarTintResource(resourceColor);
		}
	}
}
