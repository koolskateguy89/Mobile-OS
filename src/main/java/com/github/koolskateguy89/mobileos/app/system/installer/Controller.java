package com.github.koolskateguy89.mobileos.app.system.installer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.StackPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
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
		fc.setSelectedExtensionFilter(new ExtensionFilter("Zip file", "zip"));

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

	private Path getAppDir(Path name) throws IOException {
		Path appDir = Prefs.getRootDirPath().resolve(Constants.APPS_DIR).resolve(name);
		if (!Files.exists(appDir))
			Files.createDirectory(appDir);
		return appDir;
	}

	@FXML
	void zip() throws IOException {
		File file = fc.showOpenDialog(Main.getStage());

		if (file == null)
			return;

		// TODO: basically learn how to use zip
		try (ZipFile zip = new ZipFile(file)) {
			zip.getEntry("");

			Enumeration<? extends ZipEntry> e = zip.entries();
			// Really and truly, truly and really; shouldn't there only be 1 entry? (the folder)
			while (e.hasMoreElements()) {
				ZipEntry entry = e.nextElement();
				// TODO: ...
			}

			// TODO: change name to use the folder inside zip file
			Path path = file.toPath();
			Path name = path.getFileName();
			Path appDir = getAppDir(name);
		}
	}

	@FXML
	void folder() throws IOException {
		File file = dc.showDialog(Main.getStage());

		if (file == null)
			return;

		Path folder = file.toPath();
		Path name = folder.getFileName();
		Path appDir = getAppDir(name);

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
		t.setName("Installing app: " + name);
		t.setDaemon(true);
		t.start();
	}

	@Getter(value = AccessLevel.PRIVATE, lazy = true)
	private final Stage progressStage = new Stage() {{
		setTitle("Copying...");
		initOwner(Main.getStage());

		JFXProgressBar progressBar = new JFXProgressBar();
		StackPane root = new StackPane(progressBar);

		setScene(new Scene(root, 100, 50));
	}};

}
