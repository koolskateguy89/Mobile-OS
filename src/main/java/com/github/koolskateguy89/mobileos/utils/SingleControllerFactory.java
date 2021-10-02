package com.github.koolskateguy89.mobileos.utils;

import javafx.util.Callback;

import lombok.SneakyThrows;

public class SingleControllerFactory implements Callback<Class<?>, Object> {

	final Object obj;
	final Class<?> clazz;

	public SingleControllerFactory(Object obj) {
		if (obj == null)
			throw new IllegalArgumentException("obj is null");

		this.obj = obj;
		this.clazz = obj.getClass();
	}

	@Override
	@SneakyThrows
	public Object call(Class<?> param) {
		if (param == clazz) {
			return obj;
		} else {
			return param.getConstructor().newInstance();
		}
	}
}
