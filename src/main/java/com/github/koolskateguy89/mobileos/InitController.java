package com.github.koolskateguy89.mobileos;

import java.io.File;
import java.io.IOException;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.stage.DirectoryChooser;

import com.github.koolskateguy89.mobileos.utils.Utils;

public class InitController {

	private final ObjectProperty<File> folderProp = new SimpleObjectProperty<>();

	@FXML
	private Button chooseBtn;

	@FXML
	private Button doneBtn;

	@FXML
	private void initialize() {
		// Disable the doneBtn when the current folder is null
		doneBtn.disableProperty().bind(folderProp.isEqualTo(new SimpleObjectProperty<>(null)));
	}

	@FXML
	void chooseRootDir() throws IOException {
		DirectoryChooser dc = new DirectoryChooser();
		dc.setTitle("Choose root directory");
		dc.setInitialDirectory(new File("."));

		File folder;
		do {
			folder = dc.showDialog(Main.getStage());

			if (folder == null) {
				Alert a = new Alert(Alert.AlertType.ERROR, "You need to select a folder");
				a.getButtonTypes().add(ButtonType.CANCEL);

				ButtonType result = a.showAndWait().orElse(ButtonType.CANCEL);
				if (result == ButtonType.CANCEL)
					break;
			}
		} while (folder == null);

		if (folder != null) {
			folder = folder.getAbsoluteFile();
			Utils.initRootDir(folder);
		}

		this.folderProp.set(folder);
		chooseBtn.setText(String.valueOf(folder));
	}

	@FXML
	void done() throws Exception {
		Prefs.setRootDir(this.folderProp.get().getAbsolutePath());
		Main.getInstance().begin();
	}

}
