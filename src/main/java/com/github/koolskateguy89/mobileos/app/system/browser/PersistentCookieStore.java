package com.github.koolskateguy89.mobileos.app.system.browser;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import lombok.NonNull;

// It's still a bit iffy sometimes
private class PersistentCookieStore implements CookieStore {

	private final CookieStore store;

	public PersistentCookieStore(CookieStore backingStore) {
		store = backingStore;
	}

	public void store(@NonNull Path location) throws IOException {
		Map<URI, List<HttpCookie>> map = new HashMap<>();

		for (URI uri : store.getURIs()) {
			List<HttpCookie> cookies = store.get(uri);
			map.put(uri, cookies);
		}

		Properties temp = new Properties(map.size());

		Gson gson = new Gson();

		map.forEach((uri, cookies) -> {
			String json = gson.toJson(cookies);
			temp.put(uri.toString(), json);
		});

		try (OutputStream os = Files.newOutputStream(location)) {
			temp.store(os, null);
		}
	}

	public void load(@NonNull Path location) throws IOException {
		Properties temp = new Properties();
		try (InputStream is = Files.newInputStream(location)) {
			temp.load(is);
		}

		Gson gson = new Gson();

		temp.forEach((Uri, listJson) -> {
			URI uri = URI.create((String) Uri);
			JsonArray list = JsonParser.parseString((String) listJson).getAsJsonArray();
			list.forEach(elem -> {
				HttpCookie cookie = gson.fromJson(elem, HttpCookie.class);
				store.add(uri, cookie);
			});
		});
	}

	@Override
	public void add(URI uri, @NonNull HttpCookie cookie) {
		store.add(uri, cookie);
	}

	@Override
	public List<HttpCookie> get(@NonNull URI uri) {
		return store.get(uri);
	}

	@Override
	public List<HttpCookie> getCookies() {
		return store.getCookies();
	}

	@Override
	public List<URI> getURIs() {
		return store.getURIs();
	}

	@Override
	public boolean remove(URI uri, @NonNull HttpCookie cookie) {
		return store.remove(uri, cookie);
	}

	@Override
	public boolean removeAll() {
		return store.removeAll();
	}

}
