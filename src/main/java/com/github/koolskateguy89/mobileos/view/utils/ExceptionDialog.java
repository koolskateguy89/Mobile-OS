package com.github.koolskateguy89.mobileos.view.utils;

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
public class ExceptionDialog extends Dialog<ButtonType> {

	public final ButtonType COPY = new ButtonType("Copy", ButtonBar.ButtonData.OK_DONE);

	private static final String DEFAULT_TITLE = "Error";

	private final String exceptionText;

	public ExceptionDialog(Throwable exception, String message) {
		this(exception, DEFAULT_TITLE, message);
	}

	public ExceptionDialog(Throwable exception, String title, String message) {
		DialogPane dialogPane = getDialogPane();

		setTitle(title);

		dialogPane.getStyleClass().add("exception-dialog");
		dialogPane.getStylesheets().add(ProgressDialog.class.getResource("dialogs.css").toExternalForm());
		dialogPane.getButtonTypes().addAll(COPY, ButtonType.OK);

		// --- content
		dialogPane.setHeaderText(message);
		dialogPane.setContentText("The exception stacktrace was:");

		// --- expandable content
		exceptionText = Throwables.getStackTraceAsString(exception);

		TextArea textArea = new TextArea(exceptionText);
		textArea.setEditable(false);

		AnchorPane.setBottomAnchor(textArea, 0.0);
		AnchorPane.setLeftAnchor(textArea, 0.0);
		AnchorPane.setRightAnchor(textArea, 0.0);
		AnchorPane.setTopAnchor(textArea, 0.0);

		AnchorPane root = new AnchorPane(textArea);
		root.setMaxWidth(Double.MAX_VALUE);

		dialogPane.setExpandableContent(root);
	}

	// Use this because showAndWait is final ffs
	public Optional<ButtonType> showAndWaitCopy() {
		Optional<ButtonType> result = super.showAndWait();

		if (result.orElse(null) == COPY) {
			Utils.copyToClipboard(exceptionText);
		}

		return result;
	}

}
