package org.lechuga.mql.typesafe;

public class FieldDef<T, E> {

	public final Class<E> entityClass;
	public final String propertyName;

	public FieldDef(Class<E> entityClass, String propertyName) {
		super();
		this.entityClass = entityClass;
		this.propertyName = propertyName;
	}

	@Override
	public String toString() {
		return String.format("%s#%s", entityClass, propertyName);
	}
}