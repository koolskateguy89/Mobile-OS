package com.github.koolskateguy89.mobileos.app.system.settings;

import java.util.Properties;
import java.util.prefs.Preferences;

import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import com.github.koolskateguy89.mobileos.Main;
import com.github.koolskateguy89.mobileos.app.App;

import lombok.Getter;

public class SettingsApp extends App {

	private static final Properties props = new Properties() {{
		put("name", "Settings");
		put("version", Main.VERSION);
		put("appType", "SYSTEM");
		put("backgroundColor", "white");
	}};

	public SettingsApp(Preferences prefs) {
		super(null, props);
	}

	/* Pane:
	 * Multiple accordions which represent sections (General [appearance,...], {maybe} SysApps, {maybe} apps)
	 */

	@Getter(lazy = true)
	private final Pane pane = new VBox() {{
		var children = this.getChildren();
		children.add(new Label("Yo my slime"));
		children.add(new ImageView(SettingsApp.this.getIcon()));
	}};

	@Getter(lazy = true)
	private final Image icon = new Image("images/icons/Settings.gif");

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
