package com.github.koolskateguy89.mobileos.utils;

import java.lang.reflect.Method;
import java.util.Arrays;

import lombok.NonNull;
import lombok.SneakyThrows;

/*
 * I'm frustrated of other ReflectionUtils/ReflectionHelper's not having an invokeMethod(/getMethod)
 * that includes param types & returns the return value so I write my own
 *
 * LMAO looks like I won't even need it looool ahahahah ffs
 */
public class ReflectionUtils {

	private ReflectionUtils() {}

	public static Object invoke(@NonNull Object object, String methodName, Object... params) {
		return invoke(object.getClass(), methodName, params);
	}

	@SneakyThrows
	public static Object invoke(@NonNull Class<?> clazz, Object object, @NonNull String methodName, Object... params) {
		Class<?>[] paramTypes = Arrays.stream(params).map(Object::getClass).toArray(Class[]::new);
		return getMethod(clazz, methodName, paramTypes).invoke(object, params);
	}

	@SneakyThrows
	public static Method getMethod(@NonNull Class<?> clazz, @NonNull String methodName, Class<?>... paramTypes) {
		Method method = clazz.getDeclaredMethod(methodName, paramTypes);
		method.setAccessible(true);
		return method;
	}

}
