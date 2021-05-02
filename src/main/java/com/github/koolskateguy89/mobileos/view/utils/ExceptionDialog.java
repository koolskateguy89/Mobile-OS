package com.github.koolskateguy89.mobileos.view.utils;

import java.util.Optional;

import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import com.github.koolskateguy89.mobileos.utils.Utils;
import com.google.common.base.Throwables;
import org.controlsfx.dialog.ProgressDialog;

// this is basically a copy of
public class ExceptionDialog extends Dialog<ButtonType> {

	private final ButtonType COPY = new ButtonType("Copy", ButtonBar.ButtonData.OK_DONE);

	private static final String DEFAULT_TITLE = "Error";

	private final String exceptionText;

	public ExceptionDialog(Throwable exception, String message) {
		this(exception, DEFAULT_TITLE, message);
	}

	public ExceptionDialog(Throwable exception, String title, String message) {
		DialogPane dialogPane = getDialogPane();

		setTitle(title);

		setTitle("Error");
		dialogPane.setHeaderText(message);

		//dialogPane.setHeaderText(getString("exception.dlg.header"));
		dialogPane.getStyleClass().add("exception-dialog");
		dialogPane.getStylesheets().add(ProgressDialog.class.getResource("dialogs.css").toExternalForm());
		dialogPane.getButtonTypes().addAll(COPY, ButtonType.OK);

		// --- content
		//setContentText(message);

		// --- expandable content
		exceptionText = Throwables.getStackTraceAsString(exception);

		Label label = new Label("The exception stacktrace was:");

		TextArea textArea = new TextArea(exceptionText);
		textArea.setEditable(false);
		textArea.setWrapText(true);

		textArea.setMaxWidth(Double.MAX_VALUE);
		textArea.setMaxHeight(Double.MAX_VALUE);
		GridPane.setVgrow(textArea, Priority.ALWAYS);
		GridPane.setHgrow(textArea, Priority.ALWAYS);

		GridPane root = new GridPane();
		root.setMaxWidth(Double.MAX_VALUE);
		root.add(label, 0, 0);
		root.add(textArea, 0, 1);

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
