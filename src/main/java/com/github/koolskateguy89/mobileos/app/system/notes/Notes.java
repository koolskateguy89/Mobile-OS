package com.github.koolskateguy89.mobileos.app.system.notes;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;
import java.util.prefs.Preferences;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;

import com.github.koolskateguy89.mobileos.Main;
import com.github.koolskateguy89.mobileos.app.App;
import com.github.koolskateguy89.mobileos.utils.LombokOverride;

import lombok.Getter;

public class Notes extends App {

	private static final Properties props = new Properties() {{
		put("name", "Notes");
		put("version", Main.VERSION);
		put("backgroundColor", "white");
	}};

	static Preferences prefs;
	static Path dir;

	public Notes(Path dir, Preferences prefs) {
		super(dir, props);
		Notes.dir = dir;
		Notes.prefs = prefs;
	}

	NotesController nc;

	@Getter(lazy = true) @LombokOverride
	private final Node pane = new AnchorPane() {{
		FXMLLoader loader = new FXMLLoader(Notes.this.getClass().getResource("Notes.fxml"));
		loader.setRoot(this);
		try {
			loader.load();
			nc = loader.getController();
		} catch (IOException io) {
			io.printStackTrace();
		}

		StringBinding detailBinding = Bindings.createStringBinding(() -> {
			Note currentNote = nc.currentNote.get();
			return currentNote == null ? "" : " - " + currentNote.getTitle();
		}, nc.currentNote);
		Notes.this.detailProperty.bind(detailBinding);
	}};

	@Getter @LombokOverride
	// Icon made by Freepik (www.freepik.com) from www.flaticon.com
	private final Image icon = new Image("https://image.flaticon.com/icons/png/512/4021/4021693.png");

	@Override
	public void onOpen() {
	}

	@Override
	public void goBack(ActionEvent event) {
		nc.goBack();
	}

	@Override
	public void onClose() {
		nc.onClose();
	}

}
