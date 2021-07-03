package com.github.koolskateguy89.mobileos.app.system.settings;

import java.io.IOException;
import java.util.Properties;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;

import com.github.koolskateguy89.mobileos.Main;
import com.github.koolskateguy89.mobileos.app.App;
import com.github.koolskateguy89.mobileos.utils.LombokOverride;
import com.github.koolskateguy89.mobileos.utils.Utils;

import lombok.Getter;

public final class SettingsApp extends App {

	private static final Properties props = new Properties() {{
		put("name", "Settings");
		put("version", Main.VERSION);
		put("backgroundColor", "white");
	}};

	public SettingsApp() {
		super(null, props);
	}

	/* Pane:
	 * Multiple VBox's which represent sections
	 */

	@Getter(lazy = true) @LombokOverride
	private final Node pane = new VBox() {{
		FXMLLoader loader = new FXMLLoader(Utils.getFxmlUrl("system/settings/Screen"));
		loader.setRoot(this);
		try {
			loader.load();
		} catch (IOException io) {
			io.printStackTrace();
		}
	}};

	@Getter @LombokOverride
	// Icon made by Good Ware (www.flaticon.com/authors/good-ware) from www.flaticon.com
	private final Image icon = new Image("https://image.flaticon.com/icons/png/512/1084/1084979.png");

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
