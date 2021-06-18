package com.github.koolskateguy89.mobileos.app;

import java.io.File;
import java.nio.file.Path;
import java.util.Properties;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import com.github.koolskateguy89.mobileos.Main;
import com.github.koolskateguy89.mobileos.utils.Constants;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter @EqualsAndHashCode
public abstract class App {

	public static class AppConstants {
		// Image to fallback on if icon loading doesn't work
		public static final Image FALLBACK_ICON = new Image("images/icons/fallback-application-icon.png");

		// The name of the file with an application's properties
		public static final String PROPERTIES = "info.properties";

		// The [relative] path to the application jar, defined in info.properties
		public static final String JAR_PATH = "jarPath";

		// the same as BackgroundSize.DEFAULT but `contain` is true
		static final BackgroundSize SIZE = new BackgroundSize(
				BackgroundSize.AUTO, BackgroundSize.AUTO, true, true,
				true, false
		);
	}

	protected final Path directory;

	protected final String name;

	protected final String version;

	protected final AppType appType;

	@EqualsAndHashCode.Exclude
	protected final String backgroundColor;

	// Stage title will be [name][detailProperty.get()], adding a space is up to you
	protected final StringProperty detailProperty = new SimpleStringProperty("");

	// trying to replicate Javascript IIFE lmao
	@Getter(lazy = true) @EqualsAndHashCode.Exclude
	private final Button button = new Button() {{
		//<editor-fold desc="BackgroundFill[] fills">
		BackgroundFill[] fills = {
																// Rounded corners
				new BackgroundFill(Color.valueOf(backgroundColor), new CornerRadii(.10, true), null),
		};
		//</editor-fold>
		//<editor-fold desc="BackgroundImage[] images">
		Image icon = App.this.getIcon();
		if (icon == null || icon.isError())
			icon = AppConstants.FALLBACK_ICON;

		BackgroundImage[] images = {
				new BackgroundImage(
						icon, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
						BackgroundPosition.CENTER, AppConstants.SIZE
				),
		};
		//</editor-fold>

		Background bg = new Background(fills, images);
		setBackground(bg);

		// Try and take up all space it can in its parent
		setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		// Open the app when button is pressed
		setOnAction(e -> Main.getInstance().openApp(App.this));
	}};

	@Getter(lazy = true) @EqualsAndHashCode.Exclude
	private final Node node = new VBox(5) {{
		Button button = App.this.getButton();
		VBox.setVgrow(button, Priority.ALWAYS);

		Label label = new Label(App.this.getName());
		label.setTextFill(Color.WHITE);
		label.setFont(Constants.BOLD);

		getChildren().addAll(button, label);
		setAlignment(Pos.CENTER);
	}};


	// including Preferences in child constructor is optional
	/**
	 * @param directory yo my slime
	 * @param props the properties object holding information about this app
	 */
	protected App(@Nullable Path directory, @Nonnull Properties props) {
		this.directory = directory;

		name = props.getProperty("name");
		version = props.getProperty("version");
		appType = AppType.valueOf(props.getProperty("appType"));

		backgroundColor = (String) props.getOrDefault("backgroundColor", "white");
	}


	public Path getPath(String str) {
		return directory.resolve(str);
	}

	public Path getPath(Path path) {
		return directory.resolve(path);
	}

	public File getFile(String str) {
		return directory.resolve(str).toFile();
	}

	public File getFile(Path path) {
		return directory.resolve(path).toFile();
	}

	/**
	 * Use this to get an image from the app's directory.
	 *
	 * For example if app's icon is '{@code ./resources/Icon.png}' relative to the directory, you could use
	 * <pre>{@code
	 *   @lombok.Getter(lazy = true)
	 *   private final Image icon = super.getImageFromDirectory("resources/Icon.png");
	 * }</pre>
	 *
	 * @param path relative path of image
	 *
	 * @return the {@code Image}
	 */
	public final Image getImageFromDirectory(String path) {
		return new Image("file:" + directory.resolve(path));
	}

	/**
	 * @return an {@code Image} representing this app's icon
	 */
	public abstract Image getIcon();

	/**
	 * @return a {@code Node} representing the pane showing this (? - nonsensical?)
	 */
	public abstract Node getPane();

	// Called when application is opened, this should basically be opposite/inverse to onClose()
	// Called just after getPane()
	public abstract void onOpen();

	// Called when another application is opened
	public abstract void onClose();

	// Called when Back button is pressed
	public abstract void goBack(ActionEvent event);

}
