package com.github.koolskateguy89.mobileos;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import lombok.Getter;

// basically a read-only static implementation of `java.util.prefs.Preferences`
public class Prefs {

	private Prefs() {}

	private static final Preferences prefs = Preferences.userNodeForPackage(Prefs.class);

	private static final Preferences appPrefsParent = prefs.node("apps");
	public static final Preferences sysAppPrefsParent = prefs.node("sys_apps");   // TODO: give better name

	// TODO: make this non-public once Apps.fromPath(Path) is moved to Main
	public static Preferences forApp(String name) {
		return appPrefsParent.node(name);
	}
	static Preferences forSystemApp(String name) {
		return sysAppPrefsParent.node(name);
	}

	public static final boolean IS_FIRST_RUN = prefs.getBoolean("first_run", true);

	@Getter
	static String rootDir = prefs.get("root_dir", "./mobileos_root");

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
