package com.github.koolskateguy89.mobileos.app.system.installer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.StackPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import com.github.koolskateguy89.mobileos.Main;
import com.github.koolskateguy89.mobileos.Prefs;
import com.github.koolskateguy89.mobileos.app.App;
import com.github.koolskateguy89.mobileos.app.Apps;
import com.github.koolskateguy89.mobileos.utils.Constants;
import com.github.koolskateguy89.mobileos.utils.Utils;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXProgressBar;

import lombok.AccessLevel;
import lombok.Getter;

class Controller {

	static final FileChooser fc = new FileChooser();
	static final DirectoryChooser dc = new DirectoryChooser();

	static {
		File here = new File(".");
		fc.setInitialDirectory(here);
		dc.setInitialDirectory(here);
	}

	@FXML
	private JFXButton zipButton;

	@FXML
	private JFXButton folderButton;

	private void setDisable(boolean disable) {
		zipButton.setDisable(disable);
		folderButton.setDisable(disable);
	}

	@FXML
	void zip() throws IOException {
		File file = fc.showOpenDialog(Main.getStage());

		if (file == null)
			return;

		Path zip = file.toPath();
	}

	@FXML
	void folder() throws IOException {
		File file = dc.showDialog(Main.getStage());

		if (file == null)
			return;

		Path folder = file.toPath();
		Path name = folder.getFileName();

		Path appDir = Prefs.getRootDirPath().resolve(Constants.APPS_DIR).resolve(name);
		if (!Files.exists(appDir))
			Files.createDirectory(appDir);

		Stage progressStage = getProgressStage();
		progressStage.show();
		Thread t = new Thread(() -> {
			try {
				Utils.copyFolder(folder, appDir, StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				e.printStackTrace();
			}
			Platform.runLater(() -> {
				progressStage.close();
				Alert alert = new Alert(AlertType.CONFIRMATION, "App installed. Would you like to load it?",
										ButtonType.YES, ButtonType.NO);

				ButtonType res = alert.showAndWait().orElse(ButtonType.NO);
				if (res == ButtonType.YES) {
					// init app
					try {
						App app = Apps.fromPath(appDir);
						Main.getInstance().addApp(app);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		});
		t.setName("Installing: " + name);
		t.setDaemon(true);
		t.start();
	}

	@Getter(value = AccessLevel.PRIVATE, lazy = true)
	private final Stage progressStage = new Stage() {{
		setTitle("Copying...");
		initOwner(Main.getStage());

		JFXProgressBar progressBar = new JFXProgressBar();
		StackPane root = new StackPane(progressBar);

		this.setScene(new Scene(root, 100, 50));
	}};

}
