package com.github.koolskateguy89.mobileos;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.prefs.Preferences;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringExpression;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import com.github.koolskateguy89.mobileos.app.App;
import com.github.koolskateguy89.mobileos.fx.FailedAppsDialog;
import com.github.koolskateguy89.mobileos.fx.MainController;
import com.github.koolskateguy89.mobileos.fx.home.HomeController;
import com.google.common.base.Throwables;

import lombok.Getter;

public final class Main extends Application {

	private static final String DEFAULT_TITLE = "Mobile OS";

	public static String getVersion() {
		return instance.version;
	}

	// using static init doesn't work: it doesn't find the properties file
	private final String version;
	{
		Properties properties = new Properties();
		try (InputStream is = Main.class.getResourceAsStream("application.properties")) {
			properties.load(is);
		} catch (Exception ignored) { }
		version = properties.getProperty("version", "1.0");
	}

	// Get all classes from package: https://stackoverflow.com/a/520339
	// Get all classes from current runtime: https://stackoverflow.com/a/7865124
	// The only Apps that'll be in the `JavaClassPath` will be the system apps
	private static Set<Class<? extends App>> findSystemApps() {
		Reflections reflections = new Reflections(
				new ConfigurationBuilder()
						.setUrls(ClasspathHelper.forJavaClassPath())
						.setScanners(new SubTypesScanner(true))
		);
		Set<Class<? extends App>> clazzes = reflections.getSubTypesOf(App.class);
		clazzes.removeIf(clazz -> !Modifier.isPublic(clazz.getModifiers()));
		return clazzes;
	}

	private void loadSystemApps() throws Exception {
		Set<Class<? extends App>> clazzes = findSystemApps();

		Path sysAppsPrefs = Prefs.getSysAppsDir();

		List<App> sysApps = new ArrayList<>(clazzes.size());
		for (Class<? extends App> clazz : clazzes) {
			String name = clazz.getSimpleName();

			Preferences appPrefs = Prefs.forSystemApp(name);
			Path sysAppDir = sysAppsPrefs.resolve(name);

			App app = initSysApp(clazz, sysAppDir, appPrefs);
			sysApps.add(app);
			this.systemApps.put(app.getName(), app);
		}

		// sort apps in case-insensitive name order
		sysApps.sort(Comparator.comparing(App::getName, String.CASE_INSENSITIVE_ORDER));

		hc.initSystemApps(sysApps);
	}

	private static App initSysApp(Class<? extends App> clazz, Path appDir, Preferences appPrefs) throws Exception {
		Constructor<? extends App> constructor;
		Object[] initargs;
		try {
			// without prefs
			constructor = clazz.getDeclaredConstructor();
			initargs = new Object[0];
		} catch (NoSuchMethodException nsme) {
			try {
				// with prefs
				constructor = clazz.getDeclaredConstructor(Preferences.class);
				initargs = new Object[] {appPrefs};
			} catch (NoSuchMethodException nsme1) {
				try {
					// with path, no prefs
					constructor = clazz.getDeclaredConstructor(Path.class);
					initargs = new Object[] {appDir};
				} catch (NoSuchMethodException nsme2) {
					// with path, with prefs
					constructor = clazz.getDeclaredConstructor(Path.class, Preferences.class);
					initargs = new Object[] {appDir, appPrefs};
				}

				if (!Files.isDirectory(appDir))
						Files.createDirectories(appDir);
			}
		}
		return constructor.newInstance(initargs);
	}

	private Map<String, List<Path>> loadApps() throws Exception {
		Path appsDir = Prefs.getAppsDir();

		List<App> apps = new ArrayList<>();
		//  reason, paths
		Map<String, List<Path>> failedApps = new HashMap<>();

		var ds = Files.newDirectoryStream(appsDir);
		for (Path appDir : ds) {
			App app = null;
			boolean failed = true;
			String reason = null;
			Throwable e = null;
			try {
				app = Apps.fromPath(appDir);
				apps.add(app);
				failed = false;
			} catch (NullPointerException npe) {
				// not sure about this tbh
				reason = "Properties is null";
				e = npe;
			} catch (IOException io) {
				reason = "Properties file not present/accessible";
				e = io;
			} catch (ClassNotFoundException cnfe) {
				reason = "Main app class not found";
				e = cnfe;
			} catch (ExceptionInInitializerError eiie) {
				reason = "Static initialization threw exception";
				e = eiie;
			} catch (IllegalAccessException iae) {
				reason = "App class/constructor is not public";
				e = iae;
			} catch (NoSuchMethodException nsme) {
				reason = "No constructor with valid parameters";
				e = nsme;
			} catch (InstantiationException ie) {
				reason = "Abstract app class";
				e = ie;
			} catch (InvocationTargetException ite) {
				reason = "Constructor threw exception";
				e = ite;
			}

			if (e != null) System.out.println(Throwables.getStackTraceAsString(e));
			if (failed) {
				Path name = appDir.getFileName();
				// absolute genius: https://stackoverflow.com/a/37166489
				failedApps.computeIfAbsent(reason, $ -> new ArrayList<>()).add(name);
			} else {
				this.apps.put(app.getName(), app);
			}
		}
		ds.close();

		// sort apps in case-insensitive name order
		apps.sort(Comparator.comparing(App::getName, String.CASE_INSENSITIVE_ORDER));

		hc.initApps(apps);

		return failedApps;
	}

	public void addApp(App app) {
		apps.put(app.getName(), app);
		hc.addApp(app);
	}

	private final ObservableMap<String, App> systemApps = FXCollections.observableHashMap();

	private final ObservableMap<String, App> apps = FXCollections.observableHashMap();

	@Getter
	private static Main instance;
	@Getter
	private static Stage stage;

	@Getter
	private App currentApp;

	private Scene scene;

	private VBox main;
	private AnchorPane home;

	private MainController mc;
	private HomeController hc;

	@Override
	public void start(Stage primaryStage) throws Exception {
		Main.stage = primaryStage;
		Main.instance = this;

		// TODO: I like UTILITY but I want to be able to minimize :/
		//stage.initStyle(StageStyle.UTILITY);

		stage.setOnCloseRequest(event -> {
			if (currentApp != null)
				currentApp.onClose();
		});

		stage.setTitle(DEFAULT_TITLE);
		stage.setResizable(false);
		stage.centerOnScreen();

		FXMLLoader loader = new FXMLLoader(Main.class.getResource("fx/Main.fxml"));
		main = loader.load();
		mc = loader.getController();

		loader = new FXMLLoader(HomeController.class.getResource("Home.fxml"));
		home = loader.load();
		hc = loader.getController();

		Path appsDir = Prefs.getAppsDir();
		if (Prefs.IS_FIRST_RUN || !Files.isDirectory(appsDir)) {
			Pane init = FXMLLoader.load(Main.class.getResource("fx/Init.fxml"));
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
		var failed = loadApps();
		goHome();

		if (!failed.isEmpty()) {
			//System.out.println("Apps failed: " + failed);
			stage.setOnShown($ -> {
				// TODO: settings if show failed apps
				Dialog<ButtonType> d = new FailedAppsDialog(failed);
				d.initOwner(stage);
				d.showAndWait();
			});
		}
	}

	public void back(ActionEvent event) {
		if (currentApp != null)
			currentApp.goBack(event);
	}

	public boolean isAtHome() {
		return currentApp == null;
	}

	public void goHome() {
		if (currentApp != null) {
			currentApp.onClose();
			currentApp = null;
		}

		stage.titleProperty().unbind();
		stage.setTitle(DEFAULT_TITLE);
		mc.setScreen(home);
	}

	public void openApp(App app) {
		if (currentApp != null)
			currentApp.onClose();
		currentApp = app;

		mc.setScreen(app.getPane());
		app.onOpen();

		StringExpression titleBinding = Bindings.concat(app.getName(), app.getDetailProperty());
		stage.titleProperty().bind(titleBinding);
	}

	public boolean openApp(String name) {
		App app = apps.get(name);
		if (app != null) {
			openApp(app);
			return true;
		} else {
			return false;
		}
	}

	public boolean openSystemApp(String name) {
		App app = systemApps.get(name);
		if (app != null) {
			openApp(app);
			return true;
		} else {
			return false;
		}
	}

}
