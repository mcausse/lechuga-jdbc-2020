package org.lechuga;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.lechuga.autogen.Generator;
import org.lechuga.handler.ColumnHandler;
import org.lechuga.reflect.PropertyPathAccessor;

public class PropertyConfig {

	private final boolean isPk;
	private final String columnName;
	private final ColumnHandler columnHandler;
	private final Generator generator;
	private final PropertyPathAccessor propertyChain;

	public PropertyConfig(boolean isPk, String columnName, ColumnHandler columnHandler, Generator generator,
			PropertyPathAccessor propertyChain) {
		super();
		this.isPk = isPk;
		this.columnName = columnName;
		this.columnHandler = columnHandler;
		this.generator = generator;
		this.propertyChain = propertyChain;
	}

	public String getPropertyName() {
		return propertyChain.getPropertyPath();
	}

	public String getPropertyLastName() {
		return propertyChain.getLastPartPropertyName();
	}

	public Object getJdbcValue(Object entity, int offsetIndex) {
		Object value = propertyChain.get(entity, offsetIndex);
		return columnHandler.getJdbcValue(value);
	}

	public void readValue(Object entity, ResultSet rs, int offsetIndex) throws SQLException {
		Object value = columnHandler.readValue(rs, columnName);
		propertyChain.set(entity, offsetIndex, value);
	}

	public Object getJdbcValue(Object entity) {
		return getJdbcValue(entity, 0);
	}

	public void readValue(Object entity, ResultSet rs) throws SQLException {
		readValue(entity, rs, 0);
	}

	public Class<?> getPropertyType() {
		return propertyChain.getPropertyType();
	}

	////

	public void setValue(Object targetBean, Object value) {
		propertyChain.set(targetBean, value);
	}

	public boolean isPk() {
		return isPk;
	}

	public String getColumnName() {
		return columnName;
	}

	public ColumnHandler getColumnHandler() {
		return columnHandler;
	}

	public Generator getGenerator() {
		return generator;
	}

	////

	public void setValue(Object targetBean, Object value, int offsetIndex) {
		propertyChain.set(targetBean, offsetIndex, value);
	}

	public Object getValue(Object targetBean) {
		return propertyChain.get(targetBean);
	}

	public Object getValue(Object targetBean, int offsetIndex) {
		return propertyChain.get(targetBean, offsetIndex);
	}

	@Override
	public String toString() {
		return "PropertyConfig [propertyChain=" + propertyChain.getPropertyPath() + ", columnName=" + columnName
				+ ", isPk=" + isPk + ", columnHandler=" + columnHandler + ", generator=" + generator + "]";
	}

}
