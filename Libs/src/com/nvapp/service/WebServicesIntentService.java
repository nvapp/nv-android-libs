package com.nvapp.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;

/**
 * Web Services
 */
public class WebServicesIntentService extends IntentService {

	public WebServicesIntentService() {
		super(WebServicesIntentService.class.getName());
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		ResultReceiver rr = intent.getParcelableExtra("RR");

		Bundle result = new Bundle();
		result.putString("TT", "that is 黄奇  XXXX， xysfd");

		rr.send(100, result);
	}

}
