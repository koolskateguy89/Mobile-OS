package com.github.koolskateguy89.mobileos.prefs;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

// basically a static implementation of `java.util.prefs.Preferences`
public class Prefs {

	private Prefs() {}

	private static final Preferences prefs = Preferences.userRoot().node(Prefs.class.getCanonicalName());

	public static final boolean IS_FIRST_RUN = prefs.getBoolean("FIRST_RUN", true);
	//public static final boolean IS_FIRST_RUN = true;

	public static String ROOT_DIR = prefs.get("ROOT_DIR", "./data");

	static {
		// Save preferences upon JVM shutdown
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			try {
				if (IS_FIRST_RUN)
					putBoolean("FIRST_RUN", false);

				put("DATA_DIR", ROOT_DIR);

				// saves preferences to a permanent store

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

	public static void put(String key, String value) {
		prefs.put(key, value);
	}

	public static void putInt(String key, int value) {
		prefs.putInt(key, value);
	}

	public static void putDouble(String key, double value) {
		prefs.putDouble(key, value);
	}

	public static void putBoolean(String key, boolean value) {
		prefs.putBoolean(key, value);
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
