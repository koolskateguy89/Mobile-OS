package com.github.koolskateguy89.mobileos.app.system.texteditor;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.prefs.Preferences;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import com.github.koolskateguy89.mobileos.Main;
import com.github.koolskateguy89.mobileos.app.App;
import com.github.koolskateguy89.mobileos.utils.LombokOverride;
import com.github.koolskateguy89.mobileos.utils.Utils;

import lombok.Getter;

public final class TextEditor extends App {

	private static final Properties props = new Properties() {{
		put("name", "Text Editor");
		put("version", Main.VERSION);
		put("appType", "SYSTEM");
		put("backgroundColor", "lightblue");
	}};

	public TextEditor(Preferences prefs) {
		super(null, props);
		TextEditorController.setPrefs(prefs);
	}

	@Getter(lazy = true) @LombokOverride
	private final Pane pane = new VBox() {{
		FXMLLoader loader = new FXMLLoader(Utils.getFxmlUrl("system/texteditor/Editor"));
		loader.setRoot(this);
		try {
			loader.load();
		} catch (IOException io) {
			io.printStackTrace();
		}

		nc = loader.getController();

		ObjectProperty<File> fileProp = nc.fileProperty;
		BooleanProperty changedProp = nc.changed;

		StringBinding nameBinding = Bindings.createStringBinding(() -> {
			File file = fileProp.get();

			// If no file is open
			if (file == null)
				return " - Untitled";

			String name = file.getName();

			// if file extension is .txt, only show 'actual' file name
			if (name.endsWith(".txt"))
				name = name.substring(0, name.lastIndexOf(".txt"));

			return " - " + name;
		}, fileProp);

		StringBinding changedBinding = Bindings.createStringBinding(() -> changedProp.get() ? "*" : "", changedProp);

		TextEditor.super.detailProperty.bind(nameBinding.concat(changedBinding));
	}};

	private TextEditorController nc;

	// TODO: TextEditor icon
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
	public void onClose() {
		nc.quit();
	}
}
