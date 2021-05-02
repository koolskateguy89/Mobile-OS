package com.github.koolskateguy89.mobileos.notifications;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;

import com.jfoenix.controls.JFXSnackbar;

// https://material.io/components/snackbars
public class Snackbar {

	JFXSnackbar a;

	static final String DEFAULT_BUTTON_TEXT = "OK";

	public static void make(String text, int length) {
		// button.setOnAction(handler)
		make(text, DEFAULT_BUTTON_TEXT, null, length);
	}

	public static void make(String text, String buttonText, EventHandler<ActionEvent> handler, int length) {
		// button.setOnAction(handler)
	}

}
