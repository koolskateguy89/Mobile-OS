package com.github.koolskateguy89.mobileos.app.system.browser;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;

import com.github.koolskateguy89.mobileos.Main;
import com.github.koolskateguy89.mobileos.app.App;
import com.github.koolskateguy89.mobileos.utils.LombokOverride;

import lombok.Getter;

public class Browser extends App {

	private static final Properties props = new Properties() {{
		put("name", "Browser");
		put("version", Main.VERSION);
		put("backgroundColor", "white");
	}};

	public Browser(Path dir) {
		super(dir, props);
		BrowserController.dir = dir;
	}

	@Getter(lazy = true) @LombokOverride
	private final Node pane = new TabPane() {{
		FXMLLoader loader = new FXMLLoader(getClass().getResource("Browser.fxml"));
		loader.setRoot(this);
		try {
			loader.load();
			bc = loader.getController();
		} catch (IOException io) {
			io.printStackTrace();
		}
	}};

	private BrowserController bc;

	@Getter @LombokOverride
	// Icon made by Freepik (www.freepik.com) from www.flaticon.com
	private final Image icon = new Image("https://image.flaticon.com/icons/png/512/3003/3003511.png");

	@Override
	public void onOpen() {
		bc.onOpen();
	}

	@Override
	public void goBack(ActionEvent event) {
		bc.back();
	}

	@Override
	public void onClose() {
		bc.onClose();
	}

}
