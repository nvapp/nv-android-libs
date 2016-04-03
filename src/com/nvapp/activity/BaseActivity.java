package com.nvapp.activity;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class BaseActivity extends Activity {
	protected Handler handler;
	private ProgressDialog pd = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		handler = new BaseHandler(this);
		
		System.out.println("BaseActivity...");
	}

	private static class BaseHandler extends Handler {
		private WeakReference<BaseActivity> outer;

		public BaseHandler(BaseActivity activity) {
			outer = new WeakReference<BaseActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			outer.get().handleMessage(msg);
		}
	}

	protected void handleMessage(Message msg) {

	}

	public void gotoActivity(Class<?> activityClassReference) {
		Intent i = new Intent(this, activityClassReference);

		startActivity(i);
	}

	public void turnOnProgressDialog(String title, String message) {
		pd = ProgressDialog.show(this, title, message);
	}

	public void turnOffProgressDialog() {
		pd.cancel();
	}

	public void alert(String title, String message) {
		AlertDialog alertDialog = new AlertDialog.Builder(this).create();
		alertDialog.setTitle(title);
		alertDialog.setMessage(message);
		alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
			}
		});
		alertDialog.show();
	}
}
