package org.lechuga.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PropertyPathAccessor {

	static class Accessor {

		private final boolean byField;
		private final Class<?> type;
		private final Field field;
		private final Method getter;
		private final Method setter;

		public Accessor(Field field) {
			super();
			this.byField = true;
			this.type = field.getType();
			this.field = field;
			this.getter = null;
			this.setter = null;
		}

		public Accessor(Field field, Method getter, Method setter) {
			super();
			this.byField = false;
			this.type = getter.getReturnType();
			this.field = field;
			this.getter = getter;
			this.setter = setter;
		}

		public String getName() {
			return field.getName();
		}

		public Class<?> getType() {
			return type;
		}

		public Object get(Object targetBean) {
			if (byField) {
				try {
					return field.get(targetBean);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					throw new RuntimeException("getting value of the field: " + field, e);
				}
			} else {
				try {
					return getter.invoke(targetBean);
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					throw new RuntimeException("invoking the getter: " + getter, e);
				}
			}
		}

		public void set(Object targetBean, Object value) {
			if (byField) {
				try {
					field.set(targetBean, value);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					throw new RuntimeException("setting value to the field: " + field + ", value=" + value, e);
				}
			} else {
				try {
					setter.invoke(targetBean, value);
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					throw new RuntimeException("invoking the setter: " + setter + ", with value=" + value, e);
				}
			}
		}

		public Map<Class<? extends Annotation>, Annotation> getAnnotations() {
			Map<Class<? extends Annotation>, Annotation> r = new LinkedHashMap<>();
			for (Annotation a : field.getAnnotations()) {
				r.put(a.annotationType(), a);
			}
			if (!byField) {
				for (Annotation a : setter.getAnnotations()) {
					r.put(a.annotationType(), a);
				}
				for (Annotation a : getter.getAnnotations()) {
					r.put(a.annotationType(), a);
				}
			}
			return r;
		}

		@Override
		public String toString() {
			return String.format("Accessor [%s#%s]", type.getName(), field.getName());
		}

	}

	final Class<?> beanClass;
	final String propertyPath;

	final List<Accessor> accessors;

	public PropertyPathAccessor(Class<?> beanClass, String propertyPath) {
		super();
		this.beanClass = beanClass;
		this.propertyPath = propertyPath;
		this.accessors = new ArrayList<>();

		String[] propertyPathArray = propertyPath.split("\\.");
		Class<?> c = beanClass;
		for (String propertyName : propertyPathArray) {

			Field field = PropertyPathAccessorUtils.getField(c, propertyName);
			try {
				Method getter = PropertyPathAccessorUtils.getGetterMethod(c, propertyName);
				Method setter = PropertyPathAccessorUtils.getSetterMethod(c, propertyName);
				this.accessors.add(new Accessor(field, getter, setter));
			} catch (Exception e) {
				this.accessors.add(new Accessor(field));
			}
			c = field.getType();
		}
	}

	public Map<Class<? extends Annotation>, Annotation> getAnnotations() {
		Map<Class<? extends Annotation>, Annotation> r = new LinkedHashMap<>();
		for (int i = accessors.size() - 1; i >= 0; i--) {
			Accessor acc = accessors.get(i);
			r.putAll(acc.getAnnotations());
		}
		return r;
	}

	public Object get(Object bean) {
		return get(bean, 0);
	}

	public void set(Object bean, Object value) {
		set(bean, 0, value);
	}

	public Object get(Object bean, int propertyOffset) {
		try {
			Object o = bean;
			for (int i = propertyOffset; i < accessors.size(); i++) {
				Accessor accessor = accessors.get(i);

				o = accessor.get(o);
				if (o == null) {
					return null;
				}
			}
			return o;
		} catch (Exception e) {
			throw new RuntimeException("getting from " + propertyPath + " on " + bean, e);
		}
	}

	public void set(Object bean, int propertyOffset, Object value) {
		try {
			Object o = bean;
			for (int i = propertyOffset; i < accessors.size() - 1; i++) {
				Accessor accessor = accessors.get(i);

				Object newo = accessor.get(o);
				if (newo == null) {
					newo = ReflectUtils.newInstance(accessor.getType());
					accessor.set(o, newo);
				}
				o = newo;
			}

			Accessor accessor = accessors.get(accessors.size() - 1);
			accessor.set(o, value);

		} catch (Exception e) {
			throw new RuntimeException("setting to " + propertyPath + " on " + bean + ", with value=" + value, e);
		}
	}

	public Class<?> getPropertyType() {
		return accessors.get(accessors.size() - 1).getType();
	}

	public String getLastPartPropertyName() {
		return accessors.get(accessors.size() - 1).getName();
	}

	public String getPropertyPath() {
		return propertyPath;
	}

	@Override
	public String toString() {
		return String.format("PropertyPathAccessor [beanClass=%s, propertyPath=%s, annotations=%s]",
				beanClass.getName(), propertyPath, getAnnotations().values());
	}

}
