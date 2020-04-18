package org.lechuga;

import java.util.ArrayList;
import java.util.List;

import org.lechuga.handler.ColumnHandler;
import org.lechuga.handler.Handlers;

public class A {

	public static class Field<T> {

		final Class<T> fieldType;
		final String columnName;
		final ColumnHandler columnHandler;

		public Field(Class<T> fieldType, String columnName, ColumnHandler columnHandler) {
			super();
			this.fieldType = fieldType;
			this.columnName = columnName;
			this.columnHandler = columnHandler;
		}

	}

	public static class Entity<E> {

		final Class<E> entityClass;
		final List<Field<?>> id;
		final List<Field<?>> regularFields;
		final List<Field<?>> allFields;

		public Entity(Class<E> entityClass) {
			super();
			this.entityClass = entityClass;
			this.id = new ArrayList<Field<?>>();
			this.regularFields = new ArrayList<Field<?>>();
			this.allFields = new ArrayList<Field<?>>();
		}

		protected void addIdField(Field<?> f) {
			this.id.add(f);
			this.allFields.add(f);
		}

		protected void addField(Field<?> f) {
			this.regularFields.add(f);
			this.allFields.add(f);
		}

	}

	public static class Dog extends Entity<Dog> {

		public static final Field<Long> id = new Field<>(Long.class, "id_dog", Handlers.LONG);
		public static final Field<String> name = new Field<>(String.class, "name", Handlers.STRING);
		public static final Field<Double> age = new Field<>(Double.class, "age", Handlers.DOUBLE);

		public Dog() {
			super(Dog.class);
			addIdField(id);
			addField(name);
			addField(age);
		}

	}

}
