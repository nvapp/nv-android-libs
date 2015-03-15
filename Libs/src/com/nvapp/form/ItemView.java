package com.nvapp.form;

import java.util.Map;

import org.json.JSONObject;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public abstract class ItemView extends LinearLayout {
	private TextView labelView;
	private String field;
	private int group;

	public ItemView(Context context, AttributeSet attrs) {
		super(context, attrs);

		initView(context);
	}

	private void initView(Context context) {
		this.labelView = this.createLabel(context, null);
		this.addView(labelView);

		this.addView(this.createElement(context, null));
	}

	public void parse(JSONObject config) {

	}

	public void prepareValidator() {

	}

	public boolean validate() {
		return true;
	}

	public TextView createLabel(Context context, Form form) {
		TextView labelView = new TextView(context);
		labelView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

		return labelView;
	}

	public abstract View createElement(Context contex, Form form);

	public abstract String getValue();

	public abstract void setValue(CharSequence value);

	public abstract Map<String, String> getKeyValue();

	public abstract void setKeyValue(Map<String, String> keyValue);

	public void setLabel(CharSequence label) {
		this.labelView.setText(label);
	}

	public CharSequence getLabel() {
		return this.labelView.getText();
	}

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

	public int getGroup() {
		return group;
	}

	public void setGroup(int group) {
		this.group = group;
	}
}
