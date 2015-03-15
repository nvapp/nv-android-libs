package com.nvapp.form;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;

public class ItemEditText extends ItemView {
	private EditText text;

	public ItemEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public View createElement(Context contex, Form form) {
		text = new EditText(contex);
		text.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

		return text;
	}

	@Override
	public String getValue() {
		return this.text.getText().toString();
	}

	@Override
	public void setValue(CharSequence value) {
		this.text.setText(value);
	}

	@Override
	public Map<String, String> getKeyValue() {
		Map<String, String> rtn = new HashMap<String, String>();
		rtn.put(this.getField(), this.getValue());

		return rtn;
	}

	@Override
	public void setKeyValue(Map<String, String> keyValue) {
		String value = keyValue.get(this.getField());

		this.setValue(value == null ? "" : value);
	}
}
