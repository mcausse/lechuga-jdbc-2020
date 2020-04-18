package org.lechuga.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class PropertyPathAccessorUtils {

	public static Field getField(final Class<?> targetClass, final String field) throws RuntimeException {
		Class<?> o = targetClass;

		while (!Object.class.equals(o)) {

			try {
				return o.getDeclaredField(field);
			} catch (NoSuchFieldException e) {
			}

			o = o.getSuperclass();
		}

		throw new RuntimeException("no field found for: " + targetClass.getName() + "#" + field);
	}

	public static Method getGetterMethod(final Class<?> targetClass, final String field) throws RuntimeException {
		Method m;
		try {
			m = targetClass.getMethod(getGetterName(field));
		} catch (final Exception e) {
			try {
				m = targetClass.getMethod(getIsGetterName(field));
			} catch (final Exception e1) {
				throw new RuntimeException("no getter found for: " + targetClass.getName() + "#" + field, e);
			}
		}
		return m;
	}

	public static Method getSetterMethod(final Class<?> targetClass, final String field) throws RuntimeException {
		Method m = null;
		try {
			final String setterName = getSetterName(field);
			for (final Method me : targetClass.getMethods()) {
				if (me.getName().equals(setterName) && me.getReturnType().equals(void.class)
						&& me.getParameterTypes().length == 1) {
					m = me;
					break;
				}
			}
			if (m == null) {
				throw new RuntimeException();
			}
		} catch (final Exception e) {
			throw new RuntimeException("no setter found for: " + targetClass.getName() + "#" + field, e);
		}
		return m;
	}

	static String getGetterName(final String propName) {
		if (Character.isUpperCase(propName.charAt(0))
				|| propName.length() > 1 && Character.isUpperCase(propName.charAt(1))) {
			return "get" + propName;
		}
		return "get" + Character.toUpperCase(propName.charAt(0)) + propName.substring(1);
	}

	static String getIsGetterName(final String propName) {
		if (Character.isUpperCase(propName.charAt(0))
				|| propName.length() > 1 && Character.isUpperCase(propName.charAt(1))) {
			return "is" + propName;
		}
		return "is" + Character.toUpperCase(propName.charAt(0)) + propName.substring(1);
	}

	static String getSetterName(final String propName) {
		if (Character.isUpperCase(propName.charAt(0))
				|| propName.length() > 1 && Character.isUpperCase(propName.charAt(1))) {
			return "set" + propName;
		}
		return "set" + Character.toUpperCase(propName.charAt(0)) + propName.substring(1);
	}
}
