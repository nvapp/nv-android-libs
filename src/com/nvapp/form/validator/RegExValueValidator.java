package com.nvapp.form.validator;

public class RegExValueValidator implements IValueValidator {
	private String regExPattern;
	private String error;
	private String hint;

	RegExValueValidator(String inRegExPattern, String errorMessage, String inHint) {
		regExPattern = inRegExPattern;
		error = errorMessage;
		hint = inHint;
	}

	@Override
	public boolean validateValue(String value) {
		if (value.matches(regExPattern) == true) {
			return true;
		}
		return false;
	}

	@Override
	public String getErrorMessage() {
		return error + ". " + hint;
	}
}
