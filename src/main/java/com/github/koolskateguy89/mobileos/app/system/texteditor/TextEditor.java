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
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;

import com.github.koolskateguy89.mobileos.Main;
import com.github.koolskateguy89.mobileos.app.App;
import com.github.koolskateguy89.mobileos.utils.LombokOverride;

import lombok.Getter;

public final class TextEditor extends App {

	private static final Properties props = new Properties() {{
		put("name", "Text Editor");
		put("version", Main.getVersion());
		put("backgroundColor", "white");
	}};

	public TextEditor(Preferences prefs) {
		super(null, props);
		TextEditorController.setPrefs(prefs);
	}

	@Getter(lazy = true) @LombokOverride
	private final Node pane = new VBox() {{
		FXMLLoader loader = new FXMLLoader(getClass().getResource("Editor.fxml"));
		loader.setRoot(this);
		try {
			loader.load();
		} catch (IOException io) {
			io.printStackTrace();
		}

		tec = loader.getController();

		ObjectProperty<File> fileProp = tec.fileProperty;
		BooleanProperty changedProp = tec.changed;

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

	private TextEditorController tec;

	@Getter @LombokOverride
	// Icon made by Smashicons (www.flaticon.com/authors/smashicons) from www.flaticon.com
	private final Image icon = new Image("https://image.flaticon.com/icons/png/512/1512/1512772.png");

	@Override
	public void onOpen() {
		tec.init();
	}

	@Override
	public void goBack(ActionEvent event) {
		tec.undo();
	}

	@Override
	public void onClose() {
		tec.quit();
	}
}
