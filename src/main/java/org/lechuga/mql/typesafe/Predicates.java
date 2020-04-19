package org.lechuga.mql.typesafe;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.lechuga.EntityConfig;
import org.lechuga.PropertyConfig;
import org.lenteja.jdbc.query.IQueryObject;
import org.lenteja.jdbc.query.QueryObject;

public class Predicates<E> implements IQueryObject {

	final EntityConfig<E> ec;
	final String alias;

	public Predicates(EntityConfig<E> ec, String alias) {
		super();
		this.ec = ec;
		this.alias = alias;
	}

	public IQueryObject aliase(FieldDef<?, ?> field) {
		PropertyConfig prop = getPropertyConfigOf(field);
		QueryObject r = new QueryObject();
		aliase(r, prop);
		return r;
	}

	protected void aliase(QueryObject r, PropertyConfig prop) {
		if (alias != null) {
			r.append(alias);
			r.append(".");
		}
		r.append(prop.getColumnName());
	}

	protected PropertyConfig getPropertyConfigOf(FieldDef<?, ?> field) {
		if (!ec.getAllPropsMap().containsKey(field.propertyName)) {
			throw new RuntimeException(
					"property not defined in entity: " + field.entityClass.getName() + "#" + field.propertyName);
		}
		PropertyConfig prop = ec.getAllPropsMap().get(field.propertyName);
		return prop;
	}

	// =======================================================================================

	protected <T> IQueryObject col(String prefix, FieldDef<?, ?> field, String suffix) {
		PropertyConfig prop = getPropertyConfigOf(field);

		QueryObject r = new QueryObject();
		r.append(prefix);
		aliase(r, prop);
		r.append(suffix);
		return r;
	}

	public IQueryObject isNull(FieldDef<?, ?> field) {
		return col("", field, " IS NULL");
	}

	public IQueryObject isNotNull(FieldDef<?, ?> field) {
		return col("", field, " IS NOT NULL");
	}

	public IQueryObject isTrue(FieldDef<?, ?> field) {
		return col("", field, "=TRUE");
	}

	public IQueryObject isFalse(FieldDef<?, ?> field) {
		return col("", field, "=FALSE");
	}

	// =======================================================================================

	protected <T> IQueryObject colOpVal(FieldDef<T, E> field, String op, T value) {
		PropertyConfig prop = getPropertyConfigOf(field);
		Object convertedValue = prop.getColumnHandler().getJdbcValue(value);

		QueryObject r = new QueryObject();
		aliase(r, prop);
		r.append(op);
		r.append("?");
		r.addArg(convertedValue);
		return r;
	}

	public <T> IQueryObject eq(FieldDef<T, E> field, T value) {
		return colOpVal(field, "=", value);
	}

	public <T> IQueryObject lt(FieldDef<T, E> field, T value) {
		return colOpVal(field, "<", value);
	}

	public <T> IQueryObject le(FieldDef<T, E> field, T value) {
		return colOpVal(field, "<=", value);
	}

	public <T> IQueryObject gt(FieldDef<T, E> field, T value) {
		return colOpVal(field, ">", value);
	}

	public <T> IQueryObject ge(FieldDef<T, E> field, T value) {
		return colOpVal(field, ">=", value);
	}

	// =======================================================================================

	protected <T, E2> IQueryObject colOpCol(FieldDef<T, E> field, String op, Predicates<E2> predicates2,
			FieldDef<T, E2> field2) {

		PropertyConfig prop = getPropertyConfigOf(field);
		PropertyConfig prop2 = getPropertyConfigOf(field2);

		QueryObject r = new QueryObject();
		aliase(r, prop);
		r.append(op);
		predicates2.aliase(r, prop2);
		return r;
	}

	public <T, E2> IQueryObject eq(FieldDef<T, E> field, Predicates<E2> predicates2, FieldDef<T, E2> field2) {
		return colOpCol(field, "=", predicates2, field2);
	}

	public <T, E2> IQueryObject lt(FieldDef<T, E> field, Predicates<E2> predicates2, FieldDef<T, E2> field2) {
		return colOpCol(field, "<", predicates2, field2);
	}

	public <T, E2> IQueryObject le(FieldDef<T, E> field, Predicates<E2> predicates2, FieldDef<T, E2> field2) {
		return colOpCol(field, "<=", predicates2, field2);
	}

	public <T, E2> IQueryObject gt(FieldDef<T, E> field, Predicates<E2> predicates2, FieldDef<T, E2> field2) {
		return colOpCol(field, ">", predicates2, field2);
	}

	public <T, E2> IQueryObject ge(FieldDef<T, E> field, Predicates<E2> predicates2, FieldDef<T, E2> field2) {
		return colOpCol(field, "<=", predicates2, field2);
	}

	// =======================================================================================

	public IQueryObject like(FieldDef<String, ?> field, ELike elike, String value) {
		PropertyConfig prop = getPropertyConfigOf(field);

		QueryObject q = new QueryObject();
		aliase(q, prop);
		q.append(" LIKE ?");
		q.addArg(elike.process(value));
		return q;
	}

	public IQueryObject ilike(FieldDef<String, ?> field, ELike elike, String value) {
		PropertyConfig prop = getPropertyConfigOf(field);

		QueryObject q = new QueryObject();
		q.append("UPPER(");
		aliase(q, prop);
		q.append(") LIKE UPPER(?)");
		q.addArg(elike.process(value));
		return q;
	}

	// =======================================================================================

	public <T> IQueryObject between(FieldDef<T, ?> field, T value1, T value2) {
		PropertyConfig prop = getPropertyConfigOf(field);
		Object convertedValue1 = prop.getColumnHandler().getJdbcValue(value1);
		Object convertedValue2 = prop.getColumnHandler().getJdbcValue(value2);

		QueryObject q = new QueryObject();
		aliase(q, prop);
		q.append(" BETWEEN ? AND ?");
		q.addArg(convertedValue1);
		q.addArg(convertedValue2);
		return q;
	}

	// =======================================================================================

	public <T> IQueryObject in(FieldDef<T, ?> field, List<T> values) {

		PropertyConfig prop = getPropertyConfigOf(field);

		QueryObject q = new QueryObject();
		aliase(q, prop);
		q.append(" IN (");
		int c = 0;
		for (T value : values) {
			Object convertedValue = prop.getColumnHandler().getJdbcValue(value);
			if (c > 0) {
				q.append(",");
			}
			c++;
			q.append("?");
			q.addArg(convertedValue);
		}
		q.append(")");
		return q;
	}

	@SuppressWarnings("unchecked")
	public <T> IQueryObject in(FieldDef<T, ?> field, T... values) {
		return in(field, Arrays.asList(values));
	}

	public <T> IQueryObject notIn(FieldDef<T, ?> field, List<T> values) {

		PropertyConfig prop = getPropertyConfigOf(field);

		QueryObject q = new QueryObject();
		aliase(q, prop);
		q.append(" NOT IN (");
		int c = 0;
		for (T value : values) {
			Object convertedValue = prop.getColumnHandler().getJdbcValue(value);
			if (c > 0) {
				q.append(",");
			}
			c++;
			q.append("?");
			q.addArg(convertedValue);
		}
		q.append(")");
		return q;

	}

	@SuppressWarnings("unchecked")
	public <T> IQueryObject notIn(FieldDef<T, ?> field, T... values) {
		return notIn(field, Arrays.asList(values));
	}

	// =======================================================================================

	@Override
	public String getQuery() {
		StringBuilder r = new StringBuilder();
		r.append(ec.getTableName());
		if (alias != null) {
			r.append(" ");
			r.append(alias);
		}
		return r.toString();
	}

	@Override
	public Object[] getArgs() {
		return new Object[] {};
	}

	@Override
	public List<Object> getArgsList() {
		return Collections.emptyList();
	}

}
