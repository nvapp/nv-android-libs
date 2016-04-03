package com.nvapp.activity;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;

import com.nvapp.form.validator.IValidator;
import com.nvapp.service.WebServicesIntentService;

public abstract class FormActivity extends BaseActivity {
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

		System.out.println("FormActivity...");
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

	protected abstract void initializeFormFields();

	@Override
	public void setContentView(int viewid) {
		super.setContentView(viewid);

		initializeFormFields();
	}

	private List<IValidator> ruleSet = new ArrayList<IValidator>();

	public void addValidator(IValidator v) {
		ruleSet.add(v);
	}

	public boolean validateForm() {
		boolean finalResult = true;
		for (IValidator v : ruleSet) {
			boolean result = v.validate();
			if (result == false) {
				finalResult = false;
			}
		}
		return finalResult;
	}
}
