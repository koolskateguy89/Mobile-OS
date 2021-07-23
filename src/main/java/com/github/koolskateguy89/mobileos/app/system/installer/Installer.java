package com.github.koolskateguy89.mobileos.app.system.installer;

import java.io.IOException;
import java.util.Properties;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;

import com.github.koolskateguy89.mobileos.Main;
import com.github.koolskateguy89.mobileos.app.App;
import com.github.koolskateguy89.mobileos.utils.LombokOverride;

import lombok.Getter;

public final class Installer extends App {

	private static final Properties props = new Properties() {{
		put("name", "AppInstaller");
		put("version", Main.VERSION);
		put("backgroundColor", "red");
	}};

	public Installer() {
		super(null, props);
	}

	private final InstallerController ic = new InstallerController();

	@Getter(lazy = true) @LombokOverride
	private final Node pane = new Pane() {{
		FXMLLoader loader = new FXMLLoader(getClass().getResource("Installer.fxml"));
		loader.setRoot(this);
		loader.setController(ic);
		try {
			loader.load();
		} catch (IOException io) {
			io.printStackTrace();
		}
	}};

	@Getter @LombokOverride
	// Icon made by Freepik (www.freepik.com) from www.flaticon.com
	private final Image icon = new Image("https://image.flaticon.com/icons/png/512/4230/4230756.png");

}
