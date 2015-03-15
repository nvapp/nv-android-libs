package com.nvapp.form;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class Form extends LinearLayout {
	private String model;
	private int tenantId;
	private LabelLocation LabelLocation;
	private int labelWidth;

	public Form(Context context) {
		super(context);
	}

	public Form(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@SuppressLint("NewApi")
	public Form(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public int getTenantId() {
		return tenantId;
	}

	public void setTenantId(int tenantId) {
		this.tenantId = tenantId;
	}

	public LabelLocation getLabelLocation() {
		return LabelLocation;
	}

	public void setLabelLocation(LabelLocation labelLocation) {
		LabelLocation = labelLocation;
	}

	public int getLabelWidth() {
		return labelWidth;
	}

	public void setLabelWidth(int labelWidth) {
		this.labelWidth = labelWidth;
	}
}
