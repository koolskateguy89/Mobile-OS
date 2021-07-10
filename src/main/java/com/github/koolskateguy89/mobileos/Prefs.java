package com.github.koolskateguy89.mobileos;

import java.nio.file.Path;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import com.github.koolskateguy89.mobileos.utils.Constants;
import com.github.koolskateguy89.mobileos.utils.Utils;

import lombok.Getter;

// basically a read-only static implementation of `java.util.prefs.Preferences`
public final class Prefs {

	private Prefs() {}

	private static final Preferences parent = Preferences.userNodeForPackage(Prefs.class);

	private static final Preferences appPrefsParent = parent.node("apps");
	private static final Preferences sysAppPrefsParent = parent.node("sys_apps");

	static Preferences forApp(String name) {
		return appPrefsParent.node(name);
	}
	static Preferences forSystemApp(String name) {
		return sysAppPrefsParent.node(name);
	}

	public static final boolean IS_FIRST_RUN = parent.getBoolean("first_run", true);

	@Getter
	private static String rootDir = parent.get("root_dir", "./mobileos_root");
	@Getter
	private static Path rootDirPath = Path.of(rootDir);

	static void initiallySetRootDir(String s) {
		rootDir = s;
		rootDirPath = Path.of(rootDir);
	}

	// TODO be used by Settings
	private static String nextRootDir;
	static void changeRootDir(String s) {
		nextRootDir = s;
	}

	public static Path getAppsDir() {
		return rootDirPath.resolve(Constants.APPS_DIR);
	}
	public static Path getSysAppsDir() {
		return rootDirPath.resolve(Constants.SYS_APPS_DIR);
	}

	static {
		// Save preferences upon JVM shutdown
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			if (IS_FIRST_RUN)
				parent.putBoolean("first_run", false);
			parent.put("root_dir", Utils.nonNullElse(nextRootDir, rootDir));

			// saves preferences & descendants to a permanent store
			try {
				parent.flush();
			} catch (BackingStoreException e) {
				e.printStackTrace();
			}
		}));
	}


	// fave apps (bottom 4)
	// data directory

	// settings (later)

	public static String[] keys() throws BackingStoreException {
		return parent.keys();
	}

	public static String get(String key, String def) {
		return parent.get(key, def);
	}

	public static int getInt(String key, int def) {
		return parent.getInt(key, def);
	}

	public static double getDouble(String key, double def) {
		return parent.getDouble(key, def);
	}

	public static boolean getBoolean(String key, boolean def) {
		return parent.getBoolean(key, def);
	}

}
