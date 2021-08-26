package com.github.koolskateguy89.mobileos.utils;

import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import lombok.NonNull;

public class ResourceBundleImpl extends ResourceBundle implements Map<String, Object> {

	private final Map<String, Object> map = new HashMap<>();

	public ResourceBundleImpl() {
	}

	public ResourceBundleImpl(Map<String, Object> map) {
		this.map.putAll(map);
	}

	@Override
	public String toString() {
		return map.toString();
	}

	@Override
	protected Object handleGetObject(@NonNull String key) {
		return map.get(key);
	}

	@Override
	public Enumeration<String> getKeys() {
		return Collections.enumeration(map.keySet());
	}

	//<editor-fold desc="Map<> methods">
	@Override
	public Object put(String key, Object value) {
		return map.put(key, value);
	}

	@Override
	public void putAll(@NonNull Map<? extends String, ?> m) {
		map.putAll(m);
	}

	@Override
	public Object get(Object key) {
		return map.get(key);
	}

	@Override
	public Object remove(Object key) {
		return map.remove(key);
	}

	@Override
	public void clear() {
		map.clear();
	}

	@Override
	public int size() {
		return map.size();
	}

	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		return map.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return map.containsValue(value);
	}

	@Override
	public Set<Entry<String, Object>> entrySet() {
		return map.entrySet();
	}

	@Override
	public Collection<Object> values() {
		return map.values();
	}
	//</editor-fold>

}
