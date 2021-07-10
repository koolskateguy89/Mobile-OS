package com.github.koolskateguy89.mobileos.fx.utils;

import java.util.Optional;

import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;

import com.github.koolskateguy89.mobileos.utils.Utils;

public abstract class CopyDialog extends Dialog<ButtonType> {

	public final ButtonType COPY = new ButtonType("Copy", ButtonBar.ButtonData.OK_DONE);

	protected String text;

	protected CopyDialog(String text) {
		this.text = text;
	}

	// Use this because showAndWait is final ffs
	public Optional<ButtonType> showAndWaitCopy() {
		Optional<ButtonType> result = super.showAndWait();

		if (result.orElse(null) == COPY)
			Utils.copyToClipboard(text);

		return result;
	}

}
