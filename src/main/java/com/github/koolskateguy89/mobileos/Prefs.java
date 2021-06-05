package com.github.koolskateguy89.mobileos;

import java.nio.file.Path;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import com.github.koolskateguy89.mobileos.utils.Constants;

import lombok.Getter;

// basically a read-only static implementation of `java.util.prefs.Preferences`
public class Prefs {

	private Prefs() {}

	private static final Preferences prefs = Preferences.userNodeForPackage(Prefs.class);

	private static final Preferences appPrefsParent = prefs.node("apps");
	private static final Preferences sysAppPrefsParent = prefs.node("sys_apps");

	static Preferences forApp(String name) {
		return appPrefsParent.node(name);
	}
	static Preferences forSystemApp(String name) {
		return sysAppPrefsParent.node(name);
	}

	public static final boolean IS_FIRST_RUN = prefs.getBoolean("first_run", true);

	@Getter
	private static String rootDir = prefs.get("root_dir", "./mobileos_root");
	static void setRootDir(String s) {
		// FIXME: this will almost definitely cause issues if I allow changing root_dir in Settings
		rootDir = s;
		rootDirPath = Path.of(rootDir);
	}
	@Getter
	static Path rootDirPath = Path.of(rootDir);

	public static Path getAppDirPath() {
		return rootDirPath.resolve(Constants.APPS_DIR);
	}

	static {
		// Save preferences upon JVM shutdown
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			if (IS_FIRST_RUN)
				prefs.putBoolean("first_run", false);
			prefs.put("root_dir", rootDir);

			// saves preferences & descendants to a permanent store
			try {
				prefs.flush();
			} catch (BackingStoreException e) {
				e.printStackTrace();
			}
		}));
	}


	// fave apps (bottom 4)
	// data directory

	// settings (later)

	public static String[] keys() throws BackingStoreException {
		return prefs.keys();
	}

	public static String get(String key, String def) {
		return prefs.get(key, def);
	}

	public static int getInt(String key, int def) {
		return prefs.getInt(key, def);
	}

	public static double getDouble(String key, double def) {
		return prefs.getDouble(key, def);
	}

	public static boolean getBoolean(String key, boolean def) {
		return prefs.getBoolean(key, def);
	}

}
