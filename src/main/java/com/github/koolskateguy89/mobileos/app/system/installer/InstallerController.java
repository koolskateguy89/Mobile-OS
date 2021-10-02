package com.github.koolskateguy89.mobileos.app.system.installer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
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

/**
 * If re-installing(/updating) apps, this basically merges the new & old folder
 */
class InstallerController {

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

	@FXML
	private JFXButton uninstallButton;

	private void setDisable(boolean disable) {
		zipButton.setDisable(disable);
		folderButton.setDisable(disable);
		uninstallButton.setDisable(disable);
	}

	private Path getAppDir(String name) throws IOException {
		return getAppDir(Path.of(name));
	}

	private Path getAppDir(Path name) throws IOException {
		Path appDir = Prefs.getAppsDir().resolve(name);
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
					Path path = Prefs.getAppsDir().resolve(entry.getName());
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
			askToLoadApp(progressStage, appDir);
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
			askToLoadApp(progressStage, appDir);
			setDisable(false);
		});
		t.setName("Installing app: " + name);
		t.setDaemon(true);
		t.start();
	}

	private static void askToLoadApp(Stage progressStage, Path appDir) {
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

	// TODO: uninstalling apps

	@FXML
	void uninstall() throws IOException {
		App app = chooseApp();
		if (app == null)
			return;

		Path dir = app.getDirectory();
		if (dir == null)
			return;

		setDisable(true);

		Stage progressStage = getProgressStage("Uninstalling...");
		progressStage.show();

		Thread t = new Thread(() -> {
			try {
				Properties p = new Properties();
				try (InputStream is = Files.newInputStream(dir.resolve(App.AppConstants.PROPERTIES))) {
					p.load(is);
				}

				String jarPath = p.getProperty(App.AppConstants.JAR_PATH);
				Path jar = dir.resolve(jarPath);
				// FIXME: The jar is being used :/, delete jar upon exit?

				FileVisitor<Path> fileVisitor = new Utils.FileDeleter() {
					@Override
					public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
						return jar.equals(file) ? FileVisitResult.CONTINUE : super.visitFile(file, attrs);
					}
				};
				Files.walkFileTree(dir, fileVisitor);
				//Files.delete(dir);
			} catch (IOException e) {
				e.printStackTrace();
			}
			Platform.runLater(progressStage::close);
			setDisable(false);
		});
		t.setName("Uninstalling app: " + app.getName());
		t.setDaemon(true);
		t.start();
	}

	private static App chooseApp() {
		Map<String, App> apps = Main.getInstance().getApps();

		ComboBox<String> cb = new ComboBox<>();
		cb.getItems().addAll(apps.keySet());

		Alert a = new Alert(AlertType.CONFIRMATION);
		a.setHeaderText("Select an app");
		a.getDialogPane().setContent(cb);

		String appName;
		do {
			if (a.showAndWait().orElse(null) != ButtonType.OK)
				return null;
			appName = cb.getSelectionModel().getSelectedItem();
		} while (appName == null);

		return apps.get(appName);
	}



	private final Stage progressStage = new Stage() {{
		initOwner(Main.getStage());
		initStyle(StageStyle.UTILITY);

		JFXProgressBar progressBar = new JFXProgressBar();
		StackPane root = new StackPane(progressBar);

		setScene(new Scene(root, 100, 50));
	}};

	private Stage getProgressStage() {
		return getProgressStage("Copying...");
	}

	private Stage getProgressStage(String title) {
		progressStage.setTitle(title);
		return progressStage;
	}

}
