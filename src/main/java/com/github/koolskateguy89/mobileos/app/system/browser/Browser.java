package com.github.koolskateguy89.mobileos.app.system.browser;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

import com.github.koolskateguy89.mobileos.Main;
import com.github.koolskateguy89.mobileos.app.App;
import com.github.koolskateguy89.mobileos.utils.LombokOverride;
import com.github.koolskateguy89.mobileos.utils.Utils;

import lombok.Getter;

public class Browser extends App {

	private static final Properties props = new Properties() {{
		put("name", "Browser");
		put("version", Main.VERSION);
		put("appType", "SYSTEM");
		put("backgroundColor", "white");
	}};

	public Browser(Path dir) {
		super(dir, props);
		BrowserController.dir = dir;
	}

	@Getter(lazy = true) @LombokOverride
	private final Pane pane = new AnchorPane() {{
		FXMLLoader loader = new FXMLLoader(Utils.getFxmlUrl("system/browser/Browser"));
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
	// TODO: browser icon
	private final Image icon = AppConstants.FALLBACK_ICON;

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
