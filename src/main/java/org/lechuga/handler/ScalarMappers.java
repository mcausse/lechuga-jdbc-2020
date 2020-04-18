package org.lechuga.handler;

import java.math.BigDecimal;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import org.lenteja.jdbc.Mapable;
import org.lenteja.jdbc.ResultSetUtils;
import org.lenteja.jdbc.exception.JdbcException;

public class ScalarMappers {

	public static final Mapable<String> STRING = (rs) -> ResultSetUtils.getString(rs);
	public static final Mapable<Boolean> BOOLEAN = (rs) -> ResultSetUtils.getBoolean(rs);
	public static final Mapable<Date> TIMESTAMP = (rs) -> ResultSetUtils.getTimestamp(rs);
	public static final Mapable<byte[]> BYTE_ARRAY = (rs) -> ResultSetUtils.getBytes(rs);

	public static final Mapable<Byte> BYTE = (rs) -> ResultSetUtils.getByte(rs);
	public static final Mapable<Short> SHORT = (rs) -> ResultSetUtils.getShort(rs);
	public static final Mapable<Integer> INTEGER = (rs) -> ResultSetUtils.getInteger(rs);
	public static final Mapable<Long> LONG = (rs) -> ResultSetUtils.getLong(rs);
	public static final Mapable<Float> FLOAT = (rs) -> ResultSetUtils.getFloat(rs);
	public static final Mapable<Double> DOUBLE = (rs) -> ResultSetUtils.getDouble(rs);
	public static final Mapable<BigDecimal> BIG_DECIMAL = (rs) -> ResultSetUtils.getBigDecimal(rs);

	public static final Mapable<Byte> PBYTE = (rs) -> rs.getByte(1);
	public static final Mapable<Short> PSHORT = (rs) -> rs.getShort(1);
	public static final Mapable<Integer> PINTEGER = (rs) -> rs.getInt(1);
	public static final Mapable<Long> PLONG = (rs) -> rs.getLong(1);
	public static final Mapable<Float> PFLOAT = (rs) -> rs.getFloat(1);
	public static final Mapable<Double> PDOUBLE = (rs) -> rs.getDouble(1);

	static final Map<Class<?>, Mapable<?>> scalarMappers = new LinkedHashMap<>();

	static {
		scalarMappers.put(String.class, STRING);
		scalarMappers.put(Date.class, TIMESTAMP);
		scalarMappers.put(byte[].class, BYTE_ARRAY);
		scalarMappers.put(BigDecimal.class, BIG_DECIMAL);

		scalarMappers.put(Boolean.class, BOOLEAN);
		scalarMappers.put(Byte.class, BYTE);
		scalarMappers.put(Short.class, SHORT);
		scalarMappers.put(Integer.class, INTEGER);
		scalarMappers.put(Long.class, LONG);
		scalarMappers.put(Float.class, FLOAT);
		scalarMappers.put(Double.class, DOUBLE);

		// scalarMappers.put(boolean.class, PBOOLEAN);
		scalarMappers.put(byte.class, PBYTE);
		scalarMappers.put(short.class, PSHORT);
		scalarMappers.put(int.class, PINTEGER);
		scalarMappers.put(long.class, PLONG);
		scalarMappers.put(float.class, PFLOAT);
		scalarMappers.put(double.class, PDOUBLE);
	}

	@SuppressWarnings("unchecked")
	public static <T> Mapable<T> getScalarMapperFor(Class<?> columnClass) {
		if (!scalarMappers.containsKey(columnClass)) {
			throw new JdbcException("no scalar mapper defined for: " + columnClass.getName());
		}
		return (Mapable<T>) scalarMappers.get(columnClass);
	}

}