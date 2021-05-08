package com.github.koolskateguy89.mobileos.app.system.explorer;

import java.io.IOException;
import java.util.Properties;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;

import lombok.Getter;

import com.github.koolskateguy89.mobileos.Main;
import com.github.koolskateguy89.mobileos.app.App;
import com.github.koolskateguy89.mobileos.utils.Utils;

public class Explorer extends App {

	private static final Properties props = new Properties() {{
		put("name", "Explorer");
		put("version", Main.VERSION);
		put("appType", "SYSTEM");
		put("backgroundColor", "white");
	}};

	public Explorer() {
		super(null, props);
	}

	@Override
	public Image getIcon() {
		return null;
	}

	@Getter(lazy = true)
	private final Pane pane = new Pane() {{
		FXMLLoader loader = new FXMLLoader(Utils.getFxmlUrl("system/explorer/Explorer"));
		loader.setRoot(this);
		try {
			loader.load();
		} catch (IOException io) {
			io.printStackTrace();
		}
	}};

	@Override
	public void onOpen() {

	}

	@Override
	public void goBack(ActionEvent event) {

	}

	@Override
	public void onExit() {

	}

	@Override
	public void onClose() {

	}
}
