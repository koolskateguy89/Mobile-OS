package com.github.koolskateguy89.mobileos.app;

import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.Properties;

import javax.annotation.Nullable;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import com.github.koolskateguy89.mobileos.Main;
import com.github.koolskateguy89.mobileos.utils.Constants;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter @EqualsAndHashCode
public abstract class App {

	protected static class AppConstants {
		// Image to fallback on if icon loading doesn't work
		public static final Image FALLBACK_ICON = new Image("images/icons/fallback-application-icon.png");

		// The name of the file with an application's properties
		public static final String PROPERTIES = "info.properties";

		// The [relative] path to the application jar
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
		if (icon.isError())
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

		// Only show image
		setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
		// Try and take up all space it can in its parent
		setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		// Open the app when button is pressed
		setOnAction(e -> Main.getInstance().openApp(App.this));
	}};

	@Getter(lazy = true) @EqualsAndHashCode.Exclude
	private final VBox node = new VBox(5) {{
		Button button = App.this.getButton();
		VBox.setVgrow(button, Priority.ALWAYS);

		Label label = new Label(App.this.getName());
		label.setTextFill(Color.WHITE);
		label.setFont(Constants.BOLD);

		getChildren().addAll(button, label);
		setAlignment(Pos.CENTER);
	}};


	// including Preferences is optional
	protected App(@Nullable Path directory, Properties props) {
		this.directory = directory;

		name = props.getProperty("name");
		version = props.getProperty("version");
		appType = AppType.valueOf(props.getProperty("appType"));

		backgroundColor = (String) props.getOrDefault("backgroundColor", "white");
	}


	protected Path getPath(String path) {
		return directory.resolve(path);
	}

	protected Path getPath(Path path) {
		return directory.resolve(path);
	}

	protected File getFile(String path) {
		return directory.resolve(path).toFile();
	}

	protected File getFile(Path path) {
		return directory.resolve(path).toFile();
	}

	// Useful for if app icon is in dir
	protected Image getImageFromDirectory(String path) throws MalformedURLException {
		return new Image(directory.resolve(path).toUri().toURL().toExternalForm());
	}


	public abstract Image getIcon();

	// returns 'home' pane? well the first pane you want to show to user
	public abstract Pane getPane();

	// Called when application is opened, this should basically be opposite/inverse to onClose()
	// Called just after getPane()
	public abstract void onOpen();

	// Called when Back button is pressed
	public abstract void goBack(ActionEvent event);

	// Called when another application is opened
	// Any extra windows or resources
	// You need to close any extra windows or
	public abstract void onExit();

	// Called when closed through Recents
	public abstract void onClose();

}
