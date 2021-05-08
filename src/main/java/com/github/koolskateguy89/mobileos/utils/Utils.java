package com.github.koolskateguy89.mobileos.utils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import javafx.animation.FadeTransition;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import org.controlsfx.control.textfield.CustomPasswordField;
import org.controlsfx.control.textfield.CustomTextField;

public class Utils {

	private Utils() {}

	static final ClassLoader CLASS_LOADER = Utils.class.getClassLoader();

	public static URL getUrl(String url) {
		return CLASS_LOADER.getResource(url);
	}

	// DON'T USE THIS EXTERNALLY
	public static URL getFxmlUrl(String name) {
		return getUrl("view/%s.fxml".formatted(name));
	}

	public static void initRootDir(File file) throws IOException {
		Path root = file.toPath();

		Path apps = root.resolve(Constants.APPS_DIR);
		if (!Files.exists(apps))
			Files.createDirectory(apps);

		// TODO: init other dirs [if any - so far none]
	}

	public static void copyToClipboard(String text) {
		ClipboardContent content = new ClipboardContent();
		content.putString(text);
		Clipboard.getSystemClipboard().setContent(content);
	}

	public static void anchor(Node node, double top, double bottom, double left, double right) {
		AnchorPane.setTopAnchor(node, top);
		AnchorPane.setBottomAnchor(node, bottom);
		AnchorPane.setLeftAnchor(node, left);
		AnchorPane.setRightAnchor(node, right);
	}

	public static void makeClearable(CustomTextField tf) {
		setupClearButtonField(tf, tf.rightProperty());
	}

	public static void makeClearable(CustomPasswordField pf) {
		setupClearButtonField(pf, pf.rightProperty());
	}

	// just straight up copied from org.controlsfx.control.textfield.TextFields.createClearableTextField
	// why don't they just make it public man
	private static final Duration FADE_DURATION = Duration.millis(350);
	private static void setupClearButtonField(TextField inputField, ObjectProperty<Node> rightProperty) {
		inputField.getStyleClass().add("clearable-field"); //$NON-NLS-1$

		Region clearButton = new Region();
		clearButton.getStyleClass().addAll("graphic"); //$NON-NLS-1$
		StackPane clearButtonPane = new StackPane(clearButton);
		clearButtonPane.getStyleClass().addAll("clear-button"); //$NON-NLS-1$
		clearButtonPane.setOpacity(0.0);
		clearButtonPane.setCursor(Cursor.DEFAULT);
		clearButtonPane.setOnMouseReleased(e -> inputField.clear());
		clearButtonPane.managedProperty().bind(inputField.editableProperty());
		clearButtonPane.visibleProperty().bind(inputField.editableProperty());

		rightProperty.set(clearButtonPane);

		final FadeTransition fader = new FadeTransition(FADE_DURATION, clearButtonPane);
		fader.setCycleCount(1);

		inputField.textProperty().addListener(new InvalidationListener() {
			@Override public void invalidated(Observable arg0) {
				String text = inputField.getText();
				boolean isTextEmpty = text == null || text.isEmpty();
				boolean isButtonVisible = fader.getNode().getOpacity() > 0;

				if (isTextEmpty && isButtonVisible) {
					setButtonVisible(false);
				} else if (!isTextEmpty && !isButtonVisible) {
					setButtonVisible(true);
				}
			}

			private void setButtonVisible( boolean visible ) {
				fader.setFromValue(visible? 0.0: 1.0);
				fader.setToValue(visible? 1.0: 0.0);
				fader.play();
			}
		});
	}

}
