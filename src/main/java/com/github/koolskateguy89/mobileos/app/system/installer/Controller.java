package com.github.koolskateguy89.mobileos.app.system.installer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Collectors;
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
import javafx.stage.StageStyle;

import com.github.koolskateguy89.mobileos.Apps;
import com.github.koolskateguy89.mobileos.Main;
import com.github.koolskateguy89.mobileos.Prefs;
import com.github.koolskateguy89.mobileos.app.App;
import com.github.koolskateguy89.mobileos.utils.Utils;
import com.google.common.io.ByteStreams;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXProgressBar;

import lombok.AccessLevel;
import lombok.Getter;

/**
 * If re-installing(/updating) apps, this basically merges
 */
class Controller {

	static final FileChooser fc = new FileChooser();
	static final DirectoryChooser dc = new DirectoryChooser();

	static {
		fc.setSelectedExtensionFilter(new ExtensionFilter("Zip archive", "*.zip"));

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

	private Path getAppDir(String name) throws IOException {
		return getAppDir(Path.of(name));
	}

	private Path getAppDir(Path name) throws IOException {
		Path appDir = Prefs.getAppDirPath().resolve(name);
		if (!Files.isDirectory(appDir))
			Files.createDirectory(appDir);
		return appDir;
	}

	/**
	 * This relies entirely on the top level of the archive being entirely folders (apps).
	 * @throws IOException exception thrown by any IO methods
	 */
	@FXML
	void zip() throws IOException {
		File file = fc.showOpenDialog(Main.getStage());

		if (file == null)
			return;

		setDisable(true);

		ZipFile zip = new ZipFile(file);

		List<ZipEntry> entries = zip.stream().collect(Collectors.toUnmodifiableList());

		if (entries.isEmpty()) {
			try (zip) {
				Alert error = new Alert(AlertType.ERROR, "Empty zip file");
				error.showAndWait();
				return;
			}
		}

		String entryName = entries.get(0).getName();
		String name = entryName.substring(0, entryName.indexOf('/'));
		//String name = zip.getName();  // gives full file path name
		Path appDir = getAppDir(name);

		Stage progressStage = getProgressStage();
		progressStage.show();

		Thread t = new Thread(() -> {
			try (zip) {
				for (ZipEntry entry : entries) {
					// entry name includes folder name
					Path path = Prefs.getAppDirPath().resolve(entry.getName());
					if (entry.isDirectory()) {
						try {
							if (!Files.isDirectory(path))
								Files.createDirectory(path);
						} catch (IOException e) {
							e.printStackTrace();
						}
						continue;
					}

					Files.deleteIfExists(path);

					try (InputStream is = zip.getInputStream(entry); OutputStream os = Files.newOutputStream(path)) {
						ByteStreams.copy(is, os);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			finishInstall(progressStage, appDir);
			setDisable(false);
		});
		t.setName("Installing app: " + name);
		t.setDaemon(true);
		t.start();
	}

	@FXML
	void folder() throws IOException {
		File file = dc.showDialog(Main.getStage());

		if (file == null)
			return;

		setDisable(true);

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
			finishInstall(progressStage, appDir);
			setDisable(false);
		});
		t.setName("Installing app: " + name);
		t.setDaemon(true);
		t.start();
	}

	// TODO: better name
	private static void finishInstall(Stage progressStage, Path appDir) {
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
	}

	@Getter(value = AccessLevel.PRIVATE, lazy = true)
	private final Stage progressStage = new Stage() {{
		setTitle("Copying...");
		initOwner(Main.getStage());
		initStyle(StageStyle.UTILITY);

		JFXProgressBar progressBar = new JFXProgressBar();
		StackPane root = new StackPane(progressBar);

		setScene(new Scene(root, 100, 50));
	}};

}
