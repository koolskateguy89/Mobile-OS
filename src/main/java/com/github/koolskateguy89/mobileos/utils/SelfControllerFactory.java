package com.github.koolskateguy89.mobileos.utils;

import javafx.util.Callback;

import lombok.SneakyThrows;

public class SelfControllerFactory implements Callback<Class<?>, Object> {

	final Object obj;
	final Class<?> clazz;

	public SelfControllerFactory(Object obj) {
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
