package com.github.koolskateguy89.mobileos.app;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import javafx.scene.layout.BackgroundSize;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import com.github.koolskateguy89.mobileos.utils.Utils;

public class Apps {

	static final Font BOLD = Font.font("System", FontWeight.BOLD, 12);

	private static final Class<?>[] parameterTypes = {Path.class, Properties.class};

	// lmao I wanna change this to just throws `Exception` but who doesn't like seeing this clusterfuck
	// I think this is dependency injection, I have no idea
	public static App fromFile(Path dir) throws IOException, ClassNotFoundException,
			NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
		try (InputStream is = Files.newInputStream(dir.resolve(App.AppConstants.PROPERTIES))) {
			Properties props = new Properties();
			props.load(is);

			String pathToMainClass = props.getProperty("pathToMainClass");
			Path jar = dir.resolve(App.AppConstants.JAR_NAME);

			// Loading a jar during runtime: https://stackoverflow.com/a/60775
			// This basically loads the app jar into the JVM
			URLClassLoader child = new URLClassLoader(
					new URL[] {jar.toUri().toURL()}, App.class.getClassLoader()
			);

			Class<App> appClass = (Class<App>) Class.forName(pathToMainClass, true, child);
			return Utils.instantiate(appClass, parameterTypes, dir, props);
		}
	}

}
