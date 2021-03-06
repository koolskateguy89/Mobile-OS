package com.github.koolskateguy89.mobileos;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.prefs.Preferences;

import com.github.koolskateguy89.mobileos.app.App;

public class Apps {

	public static final Class<?>[] parameterTypes = {
			Path.class,
			Properties.class,
	};

	public static final Class<?>[] parameterTypesPrefs = {
			Path.class,
			Properties.class,
			Preferences.class,
	};

	public static App fromPath(Path dir) throws IOException, ClassNotFoundException, ExceptionInInitializerError,
			NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {

		Properties props = new Properties();
		try (InputStream is = Files.newInputStream(dir.resolve(App.AppConstants.PROPERTIES))) {
			props.load(is);
		}

		String name = props.getProperty("name");
		Preferences appPrefs = Prefs.forApp(name);

		String mainClassName = props.getProperty("mainClassName");

		String jarPath = props.getProperty(App.AppConstants.JAR_PATH);
		Path jar = dir.resolve(jarPath);

		// Loading a jar during runtime: https://stackoverflow.com/a/60775
		// Next 2 declarations basically load the app jar into the JVM
		URLClassLoader child = new URLClassLoader(
				new URL[] {jar.toUri().toURL()}, App.class.getClassLoader()
		);

		Class<? extends App> appClass = (Class<? extends App>) Class.forName(mainClassName, true, child);

		return instantiate(appClass, dir, props, appPrefs);
	}

	public static App instantiate(Class<? extends App> appClass, Path dir, Properties props, Preferences appPrefs)
			throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {

		Constructor<? extends App> constructor;
		Object[] initargs;
		try {
			// with prefs
			constructor = appClass.getDeclaredConstructor(parameterTypesPrefs);
			initargs = new Object[] {dir, props, appPrefs};
		} catch (NoSuchMethodException nsme) {
			// without prefs
			constructor = appClass.getDeclaredConstructor(parameterTypes);
			initargs = new Object[] {dir, props};
		}
		return constructor.newInstance(initargs);
	}

}
