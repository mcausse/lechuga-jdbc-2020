package org.lechuga;

import java.util.List;
import java.util.StringJoiner;

import org.lenteja.jdbc.query.IQueryObject;
import org.lenteja.jdbc.query.QueryObject;

public class EntityManagerOperations {

	final EntityConfig<?> entityConfig;

	final String queryForSelectAll;
	final String queryForSelectById;
	final String queryForUpdate;
	final String queryForInsert;
	final String queryForDelete;
	final String queryForDeleteById;
	final String queryForRefresh;
	final String queryForExistsById;
	final String queryForExists;

	public EntityManagerOperations(EntityConfig<?> entityConfig) {
		super();
		this.entityConfig = entityConfig;

		this.queryForSelectAll = queryForSelectAll();
		this.queryForSelectById = queryForSelectById();
		this.queryForUpdate = queryForUpdate();
		this.queryForInsert = queryForInsert();
		this.queryForDelete = queryForDelete();
		this.queryForDeleteById = queryForDeleteById();
		this.queryForRefresh = queryForRefresh();
		this.queryForExistsById = queryForExistsById();
		this.queryForExists = queryForExists();
	}

	protected String queryForSelectAll() {
		StringBuilder r = new StringBuilder();
		r.append("SELECT ");
		{
			int i = 0;
			for (PropertyConfig p : entityConfig.getAllPropsMap().values()) {
				if (i > 0) {
					r.append(",");
				}
				i++;
				r.append(p.getColumnName());
			}
		}
		r.append(" FROM ");
		r.append(entityConfig.getTableName());
		return r.toString();
	}

	public QueryObject selectAll() {
		QueryObject r = new QueryObject(queryForSelectAll);
		return r;
	}

	protected String queryForSelectById() {
		StringBuilder r = new StringBuilder();
		r.append("SELECT ");
		{
			int i = 0;
			for (PropertyConfig p : entityConfig.getAllPropsMap().values()) {
				if (i > 0) {
					r.append(",");
				}
				i++;
				r.append(p.getColumnName());
			}
		}
		r.append(" FROM ");
		r.append(entityConfig.getTableName());

		r.append(" WHERE ");

		{
			int i = 0;
			for (PropertyConfig p : entityConfig.getIdProps()) {
				if (i > 0) {
					r.append(" AND ");
				}
				i++;
				r.append(p.getColumnName());
				r.append("=?");
			}
		}
		return r.toString();
	}

	public IQueryObject selectById(Object id) {
		QueryObject r = new QueryObject(queryForSelectById);
		for (PropertyConfig p : entityConfig.getIdProps()) {
			r.addArg(p.getJdbcValue(id, 1));
		}
		return r;
	}

	protected String queryForUpdate() {
		StringBuilder r = new StringBuilder();
		r.append("UPDATE ");
		r.append(entityConfig.getTableName());
		r.append(" SET ");
		{
			int i = 0;
			for (PropertyConfig p : entityConfig.getRegularProps()) {
				if (i > 0) {
					r.append(", ");
				}
				i++;
				r.append(p.getColumnName());
				r.append("=?");
			}
		}
		r.append(" WHERE ");
		{
			int i = 0;
			for (PropertyConfig p : entityConfig.getIdProps()) {
				if (i > 0) {
					r.append(" AND ");
				}
				i++;
				r.append(p.getColumnName());
				r.append("=?");
			}
		}
		return r.toString();
	}

	public IQueryObject update(Object entity) {
		QueryObject r = new QueryObject(queryForUpdate);
		for (PropertyConfig p : entityConfig.getRegularProps()) {
			r.addArg(p.getJdbcValue(entity));
		}
		for (PropertyConfig p : entityConfig.getIdProps()) {
			r.addArg(p.getJdbcValue(entity));
		}
		return r;
	}

	protected String queryForInsert() {
		StringBuilder r = new StringBuilder();
		r.append("INSERT INTO ");
		r.append(entityConfig.getTableName());
		r.append(" (");
		{
			int i = 0;
			for (PropertyConfig p : entityConfig.getAllPropsMap().values()) {
				if (i > 0) {
					r.append(", ");
				}
				i++;
				r.append(p.getColumnName());
			}
		}
		r.append(") VALUES (");
		{
			for (int i = 0; i < entityConfig.getAllPropsMap().size(); i++) {
				if (i > 0) {
					r.append(", ");
				}
				r.append("?");
			}
		}
		r.append(")");
		return r.toString();
	}

	public IQueryObject insert(Object entity) {
		QueryObject r = new QueryObject(queryForInsert);
		for (PropertyConfig p : entityConfig.getAllPropsMap().values()) {
			r.addArg(p.getJdbcValue(entity));
		}
		return r;
	}

	protected String queryForDelete() {
		StringBuilder r = new StringBuilder();
		r.append("DELETE FROM ");
		r.append(entityConfig.getTableName());
		r.append(" WHERE ");
		{
			int i = 0;
			for (PropertyConfig p : entityConfig.getIdProps()) {
				if (i > 0) {
					r.append(" AND ");
				}
				i++;
				r.append(p.getColumnName());
				r.append("=?");
			}
		}
		return r.toString();
	}

	public IQueryObject delete(Object entity) {
		QueryObject r = new QueryObject(queryForDelete);
		for (PropertyConfig p : entityConfig.getIdProps()) {
			r.addArg(p.getJdbcValue(entity));
		}
		return r;
	}

	protected String queryForDeleteById() {
		StringBuilder r = new StringBuilder();
		r.append("DELETE FROM ");
		r.append(entityConfig.getTableName());
		r.append(" WHERE ");
		{
			int c = 0;
			for (PropertyConfig p : entityConfig.getIdProps()) {
				if (c > 0) {
					r.append(" AND ");
				}
				r.append(p.getColumnName());
				r.append("=?");
				c++;
			}
		}
		return r.toString();
	}

	public IQueryObject deleteById(Object id) {
		QueryObject q = new QueryObject(queryForDeleteById);
		for (PropertyConfig p : entityConfig.getIdProps()) {
			q.addArg(p.getJdbcValue(id, 1));
		}
		return q;
	}

	protected String queryForRefresh() {
		StringBuilder r = new StringBuilder();
		r.append("select ");
		{
			int c = 0;
			for (PropertyConfig p : entityConfig.getAllPropsMap().values()) {
				if (c > 0) {
					r.append(",");
				}
				r.append(p.getColumnName());
				c++;
			}
		}
		r.append(" from ");
		r.append(entityConfig.getTableName());
		r.append(" where ");
		{
			int c = 0;
			for (PropertyConfig p : entityConfig.getIdProps()) {
				if (c > 0) {
					r.append(" and ");
				}
				r.append(p.getColumnName());
				r.append("=?");
				c++;
			}
		}
		return r.toString();
	}

	public IQueryObject refresh(Object entity) {
		QueryObject q = new QueryObject(queryForRefresh);
		for (PropertyConfig p : entityConfig.getIdProps()) {
			q.addArg(p.getJdbcValue(entity));
		}
		return q;
	}

	protected String queryForExistsById() {
		StringBuilder r = new StringBuilder();
		r.append("SELECT COUNT(*) FROM ");
		r.append(entityConfig.getTableName());
		r.append(" WHERE ");
		{
			int c = 0;
			for (PropertyConfig p : entityConfig.getIdProps()) {
				if (c > 0) {
					r.append(" AND ");
				}
				r.append(p.getColumnName());
				r.append("=?");
				c++;
			}
		}
		return r.toString();
	}

	public IQueryObject existsById(Object id) {
		QueryObject r = new QueryObject(queryForExistsById);
		for (PropertyConfig p : entityConfig.getIdProps()) {
			r.addArg(p.getJdbcValue(id, 1));
		}
		return r;
	}

	protected String queryForExists() {
		StringBuilder r = new StringBuilder();
		r.append("SELECT COUNT(*) FROM ");
		r.append(entityConfig.getTableName());
		r.append(" WHERE ");
		{
			int c = 0;
			for (PropertyConfig p : entityConfig.getIdProps()) {
				if (c > 0) {
					r.append(" AND ");
				}
				r.append(p.getColumnName());
				r.append("=?");
				c++;
			}
		}
		return r.toString();
	}

	public IQueryObject exists(Object entity) {
		QueryObject r = new QueryObject(queryForExists);
		for (PropertyConfig p : entityConfig.getIdProps()) {
			r.addArg(p.getJdbcValue(entity));
		}
		return r;
	}

	public String sortBy(List<Order> orders) {
		if (orders != null && !orders.isEmpty()) {
			StringJoiner j = new StringJoiner(", ");
			for (Order o : orders) {
				PropertyConfig p = entityConfig.getAllPropsMap().get(o.getPropName());
				j.add(p.getColumnName() + o.getOrder());
			}
			return " order by " + j.toString();
		}
		return "";
	}

}
