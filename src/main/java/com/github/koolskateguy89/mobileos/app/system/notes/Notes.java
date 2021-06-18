package com.github.koolskateguy89.mobileos.app.system.notes;

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

// TODO: detailProperty - Note title
public class Notes extends App {

	private static final Properties props = new Properties() {{
		put("name", "Notes");
		put("version", Main.VERSION);
		put("appType", "SYSTEM");
		put("backgroundColor", "lime");
	}};

	public Notes(Path dir) {
		super(dir, props);
	}

	NotesController nc;

	@Getter(lazy = true) @LombokOverride
	private final Pane pane = new AnchorPane() {{
		FXMLLoader loader = new FXMLLoader(Utils.getFxmlUrl("system/notes/Notes"));
		loader.setRoot(this);
		try {
			loader.load();
			nc = loader.getController();
		} catch (IOException io) {
			io.printStackTrace();
		}
	}};

	@Getter @LombokOverride
	// TODO: notes icon
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
