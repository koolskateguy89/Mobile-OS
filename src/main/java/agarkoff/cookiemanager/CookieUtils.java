package agarkoff.cookiemanager;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jfoenix.adapters.ReflectionHelper;
import com.sun.webkit.network.CookieManager;

import lombok.NonNull;

// Adapted from:
// https://github.com/agarkoff/javafx-cookiemanager/blob/master/src/main/java/sample/CookieUtils.java
// It pretty much works, just not for the "Before you continue to Google" thing AFAIK
public class CookieUtils {

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

	static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	public static void store(CookieManager cm, @NonNull Path path) throws IOException {
		// com.sun.webkit.network.CookieStore (package private)
		Object cookieStore = ReflectionHelper.getFieldContent(cm, "store");

		// both ? are com.sun.webkit.network.Cookie (package private)
		Map<String, Map<?,?>> buckets = ReflectionHelper.getFieldContent(cookieStore, "buckets");

		Map<String, Collection<?>> cookiesToSave = new LinkedHashMap<>();

		buckets.forEach((domain, bucket) -> {
			cookiesToSave.put(domain, bucket.values());
		});

		String json = GSON.toJson(cookiesToSave);

		Files.writeString(path, json);
	}

	public static void load(CookieManager cm, @NonNull Path path) throws IOException {
		String json = Files.readString(path);

		Type type = new TypeToken<Map<String, Collection<CookieJson>>>() {}.getType();

		Map<String, Collection<CookieJson>> cookiesToLoad = GSON.fromJson(json, type);

		if (cookiesToLoad == null)
			return;

		cookiesToLoad.forEach((domain, cookies) -> {
			Map<String, List<String>> map = new LinkedHashMap<>();
			List<String> list = new ArrayList<>();
			map.put("Set-Cookie", list);

			for (CookieJson cookie : cookies) {
				list.add(format(
						cookie.name,
						cookie.value,
						cookie.domain,
						cookie.path,
						cookie.expiryTime,
						cookie.secureOnly,
						cookie.httpOnly
				));
			}

			URI uri = URI.create(String.format("http://%s/", domain));
			cm.put(uri, map);
		});
	}

	private static String format(
			final String name,
			final String value,
			final String domain,
			final String path,
			final long maxAge,
			final boolean isSecure,
			final boolean isHttpOnly) {

		// Check arguments
		if (name == null || name.length() == 0)
			throw new IllegalArgumentException("Bad cookie name (null/empty)");

		// Name is checked for legality by servlet spec, but can also be passed directly so check again for quoting
		// Per RFC6265, Cookie.name follows RFC2616 Section 2.2 token rules
		//Syntax.requireValidRFC2616Token(name, "RFC6265 Cookie name");
		// Ensure that Per RFC6265, Cookie.value follows syntax rules
		//Syntax.requireValidRFC6265CookieValue(value);

		// Format value and params
		StringBuilder buf = new StringBuilder();
		buf.append(name).append('=').append(value == null ? "" : value);

		// Append path
		if (path != null && path.length() > 0)
			buf.append(";Path=").append(path);

		// Append domain
		if (domain != null && domain.length() > 0)
			buf.append(";Domain=").append(domain);

		// Handle max-age and/or expires
		if (maxAge >= 0) {
			// Always use expires
			// This is required as some browser (M$ this means you!) don't handle max-age even with v1 cookies
			buf.append(";Expires=");
			if (maxAge == 0) {
				buf.append(formatCookieDate(0).trim());
			} else {
				buf.append(formatCookieDate(System.currentTimeMillis() + 1000L * maxAge));
			}
			buf.append(";Max-Age=");
			buf.append(maxAge);
		}

		// add the other fields
		if (isSecure) {
			buf.append(";Secure");
		}
		if (isHttpOnly) {
			buf.append(";HttpOnly");
		}

		return buf.toString();
	}

	private static final String[] DAYS = {"Sat", "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
	private static final String[] MONTHS = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec", "Jan"};

	/**
	 * Format "EEE, dd-MMM-yy HH:mm:ss 'GMT'" for cookies
	 *
	 * @param date the date in milliseconds
	 */
	private static String formatCookieDate(long date) {
		GregorianCalendar gc = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
		gc.setTimeInMillis(date);

		int day_of_week = gc.get(Calendar.DAY_OF_WEEK);
		int day_of_month = gc.get(Calendar.DAY_OF_MONTH);
		int month = gc.get(Calendar.MONTH);
		int year = gc.get(Calendar.YEAR);
		year = year % 10000;

		int epoch = (int) ((date / 1000) % (60 * 60 * 24));
		int seconds = epoch % 60;
		epoch = epoch / 60;
		int minutes = epoch % 60;
		int hours = epoch / 60;

		StringBuilder buf = new StringBuilder();

		buf.append(DAYS[day_of_week]);
		buf.append(',');
		buf.append(' ');
		append2digits(buf, day_of_month);

		buf.append('-');
		buf.append(MONTHS[month]);
		buf.append('-');
		append2digits(buf, year / 100);
		append2digits(buf, year % 100);

		buf.append(' ');
		append2digits(buf, hours);
		buf.append(':');
		append2digits(buf, minutes);
		buf.append(':');
		append2digits(buf, seconds);
		buf.append(" GMT");

		return buf.toString();
	}

	/**
	 * Append 2 digits (zero padded) to the StringBuilder
	 *
	 * @param buf the buffer to append to
	 * @param i the value to append
	 */
	private static void append2digits(StringBuilder buf, int i) {
		if (i < 100) {
			buf.append((char) (i / 10 + '0'));
			buf.append((char) (i % 10 + '0'));
		}
	}

	// same fields as Cookie
	private static final class CookieJson {
		String name;
		String value;
		long expiryTime;
		String domain;
		String path;
		//ExtendedTime creationTime;
		boolean secureOnly;
		boolean httpOnly;
	}

 }
