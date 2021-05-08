package com.github.koolskateguy89.mobileos;

import java.lang.reflect.Constructor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringExpression;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import com.github.koolskateguy89.mobileos.app.App;
import com.github.koolskateguy89.mobileos.app.Apps;
import com.github.koolskateguy89.mobileos.utils.Constants;
import com.github.koolskateguy89.mobileos.utils.Utils;
import com.github.koolskateguy89.mobileos.view.MainController;
import com.github.koolskateguy89.mobileos.view.home.HomeController;

import lombok.Getter;

public class Main extends Application {

	public static final String VERSION = "1.0";

	private static final String DEFAULT_TITLE = "Mobile OS";

	// TODO: {maybe} splash screen (reading data in from applications (name, image, etc.))

	// Get all classes from package: https://stackoverflow.com/a/520339
	// Get all classes from current runtime: https://stackoverflow.com/a/7865124
	// The only Apps that'll be in the `JavaClassPath` will be the system apps
	private static Set<Class<? extends App>> findSystemApps() {
		Reflections reflections = new Reflections(
				new ConfigurationBuilder()
						.setUrls(ClasspathHelper.forJavaClassPath())
						.setScanners(new SubTypesScanner(true))
		);
		return reflections.getSubTypesOf(App.class);
	}

	private void loadSystemApps() throws Exception {
		Set<Class<? extends App>> clazzes = findSystemApps();

		List<App> sysApps = new ArrayList<>(clazzes.size());
		for (Class<? extends App> clazz : clazzes) {
			String name = clazz.getSimpleName();
			Preferences appPrefs = Prefs.forSystemApp(name);

			App app = initSysApp(clazz, appPrefs);
			sysApps.add(app);
		}

		// sort apps in case-insensitive name order
		sysApps.sort(Comparator.comparing(App::getName, String.CASE_INSENSITIVE_ORDER));

		hc.initSystemApps(sysApps);
	}

	private static App initSysApp(Class<? extends App> clazz, Preferences appPrefs) throws Exception {
		Constructor<? extends App> constructor;
		Object[] initargs;
		try {
			// without prefs
			constructor = clazz.getDeclaredConstructor();
			initargs = new Object[0];
		} catch (NoSuchMethodException nsme) {
			// with prefs
			constructor = clazz.getDeclaredConstructor(Preferences.class);
			initargs = new Object[] {appPrefs};
		}
		return constructor.newInstance(initargs);
	}

	private void loadApps() throws Exception {
		Path appsDir = Path.of(Prefs.ROOT_DIR).resolve(Constants.APPS_DIR);

		List<App> apps = new ArrayList<>();

		var ds = Files.newDirectoryStream(appsDir);
		for (Path appDir : ds) {
			apps.add(Apps.fromPath(appDir));
		}

		// sort apps in case-insensitive name order
		apps.sort(Comparator.comparing(App::getName, String.CASE_INSENSITIVE_ORDER));

		hc.initApps(apps);
		// TODO: initFaves
	}

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
		//stage.initStyle(StageStyle.UTILITY);

		stage.setOnCloseRequest(event -> {
			if (currentApp != null) {
				currentApp.onExit();
				currentApp.onClose();
				// TODO: call all Recents apps onClose
			}
		});

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
		app.onOpen();

		StringExpression titleBinding = Bindings.concat(app.getName(), app.getDetailProperty());
		stage.titleProperty().bind(titleBinding);
	}

	static void launch0(String[] args) {
		Main.launch(args);
	}
}
