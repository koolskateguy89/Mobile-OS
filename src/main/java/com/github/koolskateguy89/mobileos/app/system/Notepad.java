package com.github.koolskateguy89.mobileos.app.system;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import com.github.koolskateguy89.mobileos.app.App;
import com.github.koolskateguy89.mobileos.app.system.notepad.NotepadController;
import com.github.koolskateguy89.mobileos.utils.Utils;

import lombok.Getter;

public class Notepad extends App {

	private static final Properties props = new Properties() {{
		put("name", "Notepad");
		put("version", "1.0");
		put("appType", "SYSTEM");
		put("backgroundColor", "red");
	}};

	public Notepad() {
		super(null, props);
	}

	@Getter(lazy = true)
	private final Pane pane = new VBox() {{
		FXMLLoader loader = new FXMLLoader(Utils.getFxmlUrl("system/notepad/Editor"));
		loader.setRoot(this);
		try {
			loader.load();
			nc = loader.getController();

			ObjectProperty<File> fileProp = nc.fileProperty();

			StringBinding nameBinding = Bindings.createStringBinding(() -> {
				File file = fileProp.get();

				// If no file is open,
				if (file == null)
					return " - Untitled";

				String name = file.getName();

				// if file extension is .txt, only show 'actual' file name
				if (name.endsWith(".txt"))
					name = name.substring(0, name.lastIndexOf(".txt"));

				return " - " + name;
			}, fileProp);

			Notepad.super.detailProperty.bind(nameBinding);

		} catch (IOException ignored) {
		}
	}};

	private NotepadController nc;

	@Override
	public Image getIcon() {
		return AppConstants.FALLBACK_ICON;
	}

	@Override
	public void onOpen() {
		nc.init();
	}

	@Override
	public void goBack(ActionEvent event) {
		nc.undo();
	}

	@Override
	public void onExit() {
		nc.quit();
	}

	@Override
	public void onClose() {
		nc.quit();  // for safety
	}
}
