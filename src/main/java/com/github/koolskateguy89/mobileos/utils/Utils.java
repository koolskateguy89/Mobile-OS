package com.github.koolskateguy89.mobileos.utils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.CopyOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import javafx.animation.FadeTransition;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
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

import com.google.common.base.Strings;

public class Utils {

	private Utils() {}

	static final ClassLoader CLASS_LOADER = Utils.class.getClassLoader();

	public static URL getUrl(String url) {
		return CLASS_LOADER.getResource(url);
	}

	// DON'T USE THIS EXTERNALLY
	public static URL getFxmlUrl(String name) {
		return getUrl("com/github/koolskateguy89/mobileos/fx/%s.fxml".formatted(name));
	}


	public static void initRootDir(File file) throws IOException {
		Path root = file.toPath();

		Path apps = root.resolve(Constants.APPS_DIR);
		if (!Files.isDirectory(apps))
			Files.createDirectories(apps);

		Path sysApps = root.resolve(Constants.SYS_APPS_DIR);
		if (!Files.isDirectory(sysApps))
			Files.createDirectories(sysApps);

		// TODO: init other dirs [if any - so far none]
	}


	// recursive copy folder: https://stackoverflow.com/a/60621544
	public static void copyFolder(Path source, Path target, CopyOption... options) throws IOException {
		Files.walkFileTree(source, new SimpleFileVisitor<>() {
			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				Files.createDirectories(target.resolve(source.relativize(dir)));
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				Files.copy(file, target.resolve(source.relativize(file)), options);
				return FileVisitResult.CONTINUE;
			}
		});
	}

	// recursively delete folder: https://stackoverflow.com/a/27917071
	public static void deleteDirectory(Path directory) throws IOException {
		clearDirectory(directory);
		Files.delete(directory);
	}

	public static void clearDirectory(Path directory) throws IOException {
		Files.walkFileTree(directory, new SimpleFileVisitor<>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				Files.delete(file);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				return FileVisitResult.CONTINUE;
			}
		});
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
	public static void setupClearButtonField(TextField inputField, ObjectProperty<Node> rightProperty) {
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


	public static boolean isNaturalNumber(String str) {
		for (char c : str.toCharArray()) {
			if (!Character.isDigit(c))
				return false;
		}
		return true;
	}

	public static ChangeListener<String> onlyAllowNaturalNumbersListener() {
		return (obs, oldVal, newVal) -> {
			if (!Strings.isNullOrEmpty(newVal) && !isNaturalNumber(newVal))
				((StringProperty) obs).setValue(oldVal);
		};
	}


	// String contains ignore case: https://stackoverflow.com/a/25379180
	public static boolean containsIgnoreCase(String src, String what) {
		final int length = what.length();
		if (length == 0)
			return true; // Empty string is contained

		final char firstLo = Character.toLowerCase(what.charAt(0));
		final char firstUp = Character.toUpperCase(what.charAt(0));

		for (int i = src.length() - length; i >= 0; i--) {
			// Quick check before calling the more expensive regionMatches() method:
			final char ch = src.charAt(i);
			if (ch != firstLo && ch != firstUp)
				continue;

			if (src.regionMatches(true, i, what, 0, length))
				return true;
		}

		return false;
	}

}
