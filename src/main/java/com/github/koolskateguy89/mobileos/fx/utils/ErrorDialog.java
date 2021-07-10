package com.github.koolskateguy89.mobileos.fx.utils;

import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;

import org.controlsfx.dialog.ProgressDialog;

import com.github.koolskateguy89.mobileos.utils.Utils;

// this is basically a very slightly tailored copy of org.controlsfx.dialog.ExceptionDialog
public class ErrorDialog extends CopyDialog {

	protected static final String DEFAULT_TITLE = "Error";

	protected final TextArea textArea;

	public ErrorDialog(String message, String details) {
		this(DEFAULT_TITLE, message, details);
	}

	public ErrorDialog(String title, String message, String details) {
		super(details);

		DialogPane dialogPane = getDialogPane();

		setTitle(title);

		dialogPane.getStyleClass().add("exception-dialog");
		dialogPane.getStylesheets().add(ProgressDialog.class.getResource("dialogs.css").toExternalForm());
		dialogPane.getButtonTypes().addAll(COPY, ButtonType.OK);

		// --- content
		dialogPane.setHeaderText(message);
		dialogPane.setContentText("The exception stacktrace was:");

		// --- expandable content
		textArea = new TextArea(details);
		textArea.setEditable(false);

		Utils.anchor(textArea, 0, 0, 0, 0);

		AnchorPane root = new AnchorPane(textArea);
		root.setMaxWidth(Double.MAX_VALUE);

		dialogPane.setExpandableContent(root);
	}

	public void setMessage(String message) {
		getDialogPane().setHeaderText(message);
	}

	public void setDetails(String details) {
		super.text = details;
		textArea.setText(details);
	}

}
