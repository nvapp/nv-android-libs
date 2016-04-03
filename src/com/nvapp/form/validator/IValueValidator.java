package com.nvapp.form.validator;

public interface IValueValidator {
	boolean validateValue(String value);

	String getErrorMessage();
}
