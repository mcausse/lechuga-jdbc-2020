package org.lechuga.reflect;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class PropertyPathAccessorScanner {

	public Map<String, PropertyPathAccessor> scan(Class<?> beanClass) {

		List<String> ls = new ArrayList<>();
		try {
			scan(beanClass, new Stack<>(), ls);
		} catch (Exception e) {
			throw new RuntimeException("scanning bean: " + beanClass.getName(), e);
		}

		//

		Map<String, PropertyPathAccessor> r = new LinkedHashMap<>();
		for (String propPath : ls) {
			r.put(propPath, new PropertyPathAccessor(beanClass, propPath));
		}

		return r;
	}

	protected void scan(Class<?> beanClass, Stack<String> propsPath, List<String> r) {

		Class<?> o = beanClass;
		while (!Object.class.equals(o)) {
			for (Field f : o.getDeclaredFields()) {

				if (f.getName().contains("$")) {
					continue;
				}

				if (f.getType().getAnnotation(Embbedable.class) != null) {
					propsPath.push(f.getName());
					scan(f.getType(), propsPath, r);
					propsPath.pop();
				} else {
					String propertyPath = getPropertyPath(propsPath, f.getName());
					r.add(propertyPath);
				}

			}
			o = o.getSuperclass();
		}

	}

	protected String getPropertyPath(List<String> propsPath, String property) {
		StringBuilder s = new StringBuilder();
		for (String p : propsPath) {
			s.append(p);
			s.append('.');
		}
		s.append(property);
		return s.toString();
	}

}
