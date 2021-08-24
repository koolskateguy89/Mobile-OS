package com.github.koolskateguy89.mobileos.app.system.counter;

import javax.xml.crypto.Data;

import org.sqlite.SQLiteDataSource;

public class Database {

	private static final SQLiteDataSource ds = new SQLiteDataSource() {{
		setUrl("jdbc:sqlite::resource:" + Database.class.getPackageName().replace('.', '/') + "/animeDB.db");
	}};

	private static final String CREATE_TABLE = """
			CREATE TABLE counts (
			    title STRING,
			    min   INTEGER,
			    max   INTEGER
			);
			""";

}
