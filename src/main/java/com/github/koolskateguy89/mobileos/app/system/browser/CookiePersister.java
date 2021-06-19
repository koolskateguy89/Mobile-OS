package com.github.koolskateguy89.mobileos.app.system.browser;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;

import org.reflections.ReflectionUtils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.jfoenix.adapters.ReflectionHelper;

import lombok.SneakyThrows;

// Annddd it still doesn't work ffs
public class CookiePersister {

	/*
	 * The CookieStore internally has a Map<String, Map<Cookie,Cookie>> buckets
	 *   - String = cookie.getDomain()
	 *   - Map<Cookie,  = the cookie
	 *   - Map<,Cookie> = the cookie (initially)
	 *
	 * Each bucket (Map<Cookie,Cookie>) stores a domain's (e.g. "google.co.uk")
	 *   cookies
	 *
	 * Initially in a bucket, key === value (identity equivalence) (the cookie),
	 *   but the value gets replaced by a cookie with the same name, domain & path
	 *   (given by Cookie.hashCode()). Often cookie.getName() = purpose.
	 *
	 *   At all points, key.equals(value) == true, but eventually they aren't the
	 *   same object.
	 *
	 */

	// use Reflections' ReflectionUtils(String) instead of Class.forName(String) because the latter throws an exception ;)
	private static final Class<?> COOKIE_CLASS = ReflectionUtils.forName("com.sun.webkit.network.Cookie");

	// com.sun.webkit.network.CookieStore (package private)
	final Object cookieStore;

	// both ? are com.sun.webkit.network.Cookie (package private)
	final Map<String, Map<?,?>> buckets;

	public CookiePersister(Object cookieStore) {
		if (!cookieStore.getClass().getCanonicalName().equals("com.sun.webkit.network.CookieStore"))
			throw new IllegalArgumentException("cookieStore not an instance of com.sun.webkit.network.CookieStore");

		this.cookieStore = cookieStore;
		buckets = ReflectionHelper.getFieldContent(cookieStore, "buckets");
	}

	public void store(Path path) throws IOException {
		Properties temp = new Properties();

		Gson gson = new Gson();

		buckets.forEach((domain, bucket) -> {
			JsonArray cookiePairs = new JsonArray(bucket.size());

			bucket.forEach((originalCookie, mostRecentCookie) -> {
				// Surely we only need the mostRecentCookie ? - tried and didn't work
				// tried using both original & mostRecent and still doesn't work :DDDDDDDDDDD

				JsonArray pair = new JsonArray(2);

				JsonElement original = gson.toJsonTree(originalCookie);
				JsonElement mostRecent = gson.toJsonTree(mostRecentCookie);

				pair.add(original);
				pair.add(mostRecent);

				cookiePairs.add(pair);
			});

			temp.put(domain, cookiePairs.toString());
		});

		try (OutputStream os = Files.newOutputStream(path)) {
			temp.store(os, null);
		}
	}

	public void load(Path path) throws IOException{
		Properties temp = new Properties();

		try (InputStream is = Files.newInputStream(path)) {
			temp.load(is);
		}

		Gson gson = new Gson();

		temp.forEach((domain, bucketJson) -> {
			JsonArray cookiePairs = JsonParser.parseString((String) bucketJson).getAsJsonArray();
			cookiePairs.forEach(pairElement -> {
				JsonArray pair = pairElement.getAsJsonArray();

				Object originalCookie = gson.fromJson(pair.get(0), COOKIE_CLASS);
				put(originalCookie);

				Object mostRecentCookie = gson.fromJson(pair.get(1), COOKIE_CLASS);
				put(mostRecentCookie);
			});
		});
	}

	public void put(Object cookie) {
		invokeMethod(cookieStore, "put", cookie);
	}

	public Object get(Object cookie) {
		return invokeMethod(cookieStore, "get", cookie);
	}
	
	@SneakyThrows({
			NoSuchMethodException.class,
			SecurityException.class,
			InvocationTargetException.class,
			IllegalAccessException.class
	})
	private static Object invokeMethod(Object object, String methodName, Object... args) {
		Class<?> clazz = object.getClass();
		Method method = clazz.getDeclaredMethod(methodName, Arrays.stream(args).map(Object::getClass).toArray(Class[]::new));
		method.setAccessible(true);
		return method.invoke(object, args);
	}

}
