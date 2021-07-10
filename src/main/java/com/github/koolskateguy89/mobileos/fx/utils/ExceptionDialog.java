package com.github.koolskateguy89.mobileos.fx.utils;

import java.util.Optional;

import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;

import org.controlsfx.dialog.ProgressDialog;

import com.github.koolskateguy89.mobileos.utils.Utils;
import com.google.common.base.Throwables;

// this is basically a tailored copy of org.controlsfx.dialog.ExceptionDialog
public class ExceptionDialog extends ErrorDialog {

	public ExceptionDialog(Throwable exception, String message) {
		this(exception, DEFAULT_TITLE, message);
	}

	public ExceptionDialog(Throwable exception, String title, String message) {
		super(title, message, Throwables.getStackTraceAsString(exception));
	}

}
