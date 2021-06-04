package com.github.koolskateguy89.mobileos.app.system.settings;

import java.io.IOException;
import java.util.Properties;
import java.util.prefs.Preferences;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import com.github.koolskateguy89.mobileos.Main;
import com.github.koolskateguy89.mobileos.app.App;
import com.github.koolskateguy89.mobileos.utils.Utils;

import lombok.Getter;

public final class SettingsApp extends App {

	private static final Properties props = new Properties() {{
		put("name", "Settings");
		put("version", Main.VERSION);
		put("appType", "SYSTEM");
		put("backgroundColor", "white");
	}};

	public SettingsApp() {
		super(null, props);
	}

	/* Pane:
	 * Multiple VBox's which represent sections
	 */

	@Getter(lazy = true)
	private final Pane pane = new VBox() {{
		FXMLLoader loader = new FXMLLoader(Utils.getFxmlUrl("system/settings/Screen"));
		loader.setRoot(this);
		try {
			loader.load();
		} catch (IOException io) {
			io.printStackTrace();
		}
	}};

	@Getter
	private final Image icon = new Image("images/icons/Settings.gif");

	@Override
	public void onOpen() {
	}

	@Override
	public void goBack(ActionEvent event) {
	}

	@Override
	public void onClose() {
	}

}
