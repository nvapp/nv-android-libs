package com.nvapp.activity;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class BaseActivity extends Activity {
	protected Handler handler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		handler = new BaseHandler(this);
	}

	protected void handleMessage(Message msg) {

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
}
