package com.github.koolskateguy89.mobileos.fx.utils;

import com.google.common.base.Throwables;

// this is basically a tailored copy of org.controlsfx.dialog.ExceptionDialog
public class ExceptionDialog extends ErrorDialog {

	public ExceptionDialog(Throwable exception, String message) {
		super(message, Throwables.getStackTraceAsString(exception));
	}

	public ExceptionDialog(Throwable exception, String title, String message) {
		super(title, message, Throwables.getStackTraceAsString(exception));
	}

}
