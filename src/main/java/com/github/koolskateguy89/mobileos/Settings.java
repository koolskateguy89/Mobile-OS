package com.github.koolskateguy89.mobileos;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.paint.Paint;

// this should include all the settings
public final class Settings {

	private Settings() {}

	public static final ObjectProperty<Background> BACKGROUND_PROPERTY = new SimpleObjectProperty<>();

	// Scale image to fit 100% of the background
	private static final BackgroundSize DEFAULT_SIZE = new BackgroundSize(
			1, 1, true, true, true, true
	);

	public static void setBackground(String imageUrl) {
		setBackground(new Image(imageUrl));
	}

	public static void setBackground(Image image) {
		BackgroundImage bgImage = new BackgroundImage(
				image, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
				BackgroundPosition.CENTER, DEFAULT_SIZE
		);
		BACKGROUND_PROPERTY.set(new Background(bgImage));
	}

	public static void setBackground(Paint fill) {
		BackgroundFill bgFill = new BackgroundFill(fill, null, null);
		BACKGROUND_PROPERTY.set(new Background(bgFill));
	}

}
