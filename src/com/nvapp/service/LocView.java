package com.nvapp.service;

import android.content.Context;
import android.widget.TextView;

public class LocView extends TextView {

	public LocView(Context context) {
		super(context);
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		
		this.getParent();
	}
}
