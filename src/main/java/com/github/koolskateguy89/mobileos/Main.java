package com.github.koolskateguy89.mobileos;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import lombok.Getter;

import com.github.koolskateguy89.mobileos.app.App;
import com.github.koolskateguy89.mobileos.app.Apps;
import com.github.koolskateguy89.mobileos.prefs.Prefs;
import com.github.koolskateguy89.mobileos.utils.Constants;
import com.github.koolskateguy89.mobileos.utils.Utils;
import com.github.koolskateguy89.mobileos.view.MainController;
import com.github.koolskateguy89.mobileos.view.home.HomeController;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

public class Main extends Application {

	// TODO: init where apps are stored and stuff (also on first run ask where stuff is to be stored

	// TODO: splash screen (reading data in from applications (name, image, etc.))

	/* Applications are to be stored as JAR files (packaged with dependencies? (excl. JavaFX & stuff I guess))
	 * which are loaded in at runtime (is this a good idea???) (https://stackoverflow.com/q/60764).
	 * If this is done, the name of the JAR==name of application, so passing the directory of the application would
	 * be easy right???
	 *
	 * No. This won't work because of persistent storage (without databases). Applications have to be folders.
	 * OR maybe it will
	 */

	private static List<App> loadAppsFrom(Path appsDir) throws Exception {
		List<App> apps = new ArrayList<>();

		var ds = Files.newDirectoryStream(appsDir);
		for (Path appDir : ds) {
			apps.add(Apps.fromFile(appDir));
		}

		return apps;
	}

	// Get all classes from package: https://stackoverflow.com/a/520339
	// Get all classes from current runtime: https://stackoverflow.com/a/7865124
	private static Set<Class<? extends App>> findSystemApps() {
		Reflections reflections = new Reflections(
				new ConfigurationBuilder()
						.setUrls(ClasspathHelper.forJavaClassPath())
						.setScanners(new SubTypesScanner(true))
		);
		return reflections.getSubTypesOf(App.class);
	}

	public void loadSystemApps() throws Exception {
		Set<Class<? extends App>> clazzes = findSystemApps();

		List<App> sysApps = new ArrayList<>(clazzes.size());
		for (Class<? extends App> clazz : clazzes)
			sysApps.add(Utils.instantiate(clazz));

		// sort apps in case-insensitive name order
		sysApps.sort(Comparator.comparing(App::getName, String.CASE_INSENSITIVE_ORDER));

		hc.initSystemApps(sysApps);
	}

	public void loadApps() throws Exception {
		Path appsDir = Path.of(Prefs.ROOT_DIR).resolve(Constants.APPS_DIR);
		List<App> apps = loadAppsFrom(appsDir);

		// sort apps in case-insensitive name order
		apps.sort(Comparator.comparing(App::getName, String.CASE_INSENSITIVE_ORDER));

		hc.initApps(apps);
		// TODO: initFaves
	}

	private static final String DEFAULT_TITLE = "Mobile OS";

	@Getter
	private static Main instance;
	@Getter
	private static Stage stage;

	@Getter
	private App currentApp;

	private Scene scene;

	private VBox main;
	private VBox home;

	private MainController mc;
	private HomeController hc;

	@Override
	public void start(Stage primaryStage) throws Exception {
		Main.stage = primaryStage;
		Main.instance = this;

		// I like UTILITY but I want to be able to minimize :/
		stage.initStyle(StageStyle.UTILITY);

		stage.setTitle(DEFAULT_TITLE);
		stage.setResizable(false);
		stage.centerOnScreen();

		FXMLLoader loader = new FXMLLoader(Utils.getFxmlUrl("Main"));
		main = loader.load();
		mc = loader.getController();

		loader = new FXMLLoader(Utils.getFxmlUrl("home/Home"));
		home = loader.load();
		hc = loader.getController();

		if (Prefs.IS_FIRST_RUN) {
			Pane init = FXMLLoader.load(Utils.getFxmlUrl("Init"));
			scene = new Scene(init);
		} else {
			scene = new Scene(main);
			begin();
		}

		stage.setScene(scene);
		stage.sizeToScene();
		stage.show();
	}

	public void begin() throws Exception {
		scene.setRoot(main);
		loadSystemApps();
		loadApps();
		goHome();
	}

	public void back(ActionEvent event) {
		if (currentApp != null)
			currentApp.goBack(event);
		// home screen 'doesnt' go back - well i cba
	}

	public void goHome() {
		if (currentApp != null) {
			currentApp.onExit();
			currentApp = null;
		}
		stage.titleProperty().unbind();
		stage.setTitle(DEFAULT_TITLE);
		mc.setScreen(home);
	}

	public void openApp(App app) {
		if (currentApp != null)
			currentApp.onExit();
		currentApp = app;

		mc.setScreen(app.getPane());
		app.open();

		stage.titleProperty().bind(
				Bindings.concat(app.getName(), app.getDetailProperty())
		);
	}

	static void launch0(String[] args) {
		Main.launch(args);
	}
}
