package com.github.koolskateguy89.mobileos.app.system.browser;

import java.io.IOException;
import java.util.Properties;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

import com.github.koolskateguy89.mobileos.Main;
import com.github.koolskateguy89.mobileos.app.App;
import com.github.koolskateguy89.mobileos.utils.Utils;

import lombok.Getter;

public class Browser extends App {

	private static final Properties props = new Properties() {{
		put("name", "Browser");
		put("version", Main.VERSION);
		put("appType", "SYSTEM");
		put("backgroundColor", "white");
	}};

	public Browser() {
		super(null, props);
	}

	@Getter(lazy = true)
	private final Pane pane = new AnchorPane() {{
		FXMLLoader loader = new FXMLLoader(Utils.getFxmlUrl("system/browser/Browser"));
		loader.setRoot(this);
		try {
			loader.load();
		} catch (IOException io) {
			io.printStackTrace();
		}
	}};

	@Getter
	// TODO: browser icon
	private final Image icon = AppConstants.FALLBACK_ICON;

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
