package com.nvapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;

import com.nvapp.service.WebServicesIntentService;

public class FormActivity extends BaseActivity {
	private ResultReceiver resultReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		resultReceiver = new ResultReceiver(this.handler) {
			@Override
			protected void onReceiveResult(int resultCode, Bundle resultData) {
				FormActivity.this.onReceiveResult(resultCode, resultData);
			}
		};
	}

	protected void onReceiveResult(int resultCode, Bundle resultData) {
		System.out.println(resultCode);
		System.out.println(resultData.getString("TT"));
	}

	protected void doPost() {
		Intent intent = new Intent(this, WebServicesIntentService.class);
		intent.putExtra("RR", resultReceiver);

		startService(intent);
	}
}
