package com.github.koolskateguy89.mobileos.app.system.installer;

import java.io.IOException;
import java.util.Properties;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;

import lombok.Getter;

import com.github.koolskateguy89.mobileos.Main;
import com.github.koolskateguy89.mobileos.app.App;
import com.github.koolskateguy89.mobileos.utils.Utils;

public class Installer extends App {

	private static final Properties props = new Properties() {{
		put("name", "AppInstaller");
		put("version", Main.VERSION);
		put("appType", "SYSTEM");
		put("backgroundColor", "red");
	}};

	public Installer() {
		super(null, props);
	}

	private final Controller controller = new Controller();

	@Getter(lazy = true)
	private final Pane pane = new Pane() {{
		FXMLLoader loader = new FXMLLoader(Utils.getFxmlUrl("system/installer/Installer"));
		loader.setRoot(this);
		loader.setController(controller);
		try {
			loader.load();
		} catch (IOException io) {
			io.printStackTrace();
		}
	}};

	// TODO:
	@Override
	public Image getIcon() {
		return null;
	}

	@Override
	public void onOpen() {
		// no-op
	}

	@Override
	public void goBack(ActionEvent event) {
		// no-op
	}

	@Override
	public void onExit() {
		// no-op
	}

	@Override
	public void onClose() {
		// no-op
	}
}
