package org.lechuga;

import java.util.List;
import java.util.StringJoiner;

import org.lenteja.jdbc.query.IQueryObject;
import org.lenteja.jdbc.query.QueryObject;

public class EntityManagerOperations {

	public QueryObject queryForSelectAll(EntityConfig<?> emc) {
		QueryObject r = new QueryObject();
		r.append("SELECT ");
		{
			int i = 0;
			for (PropertyConfig p : emc.getAllPropsMap().values()) {
				if (i > 0) {
					r.append(",");
				}
				i++;
				r.append(p.getColumnName());
			}
		}
		r.append(" FROM ");
		r.append(emc.getTableName());
		return r;
	}

	// public void renderOrderBy(EntityConfig<?> emc, List<Order> order, QueryObject
	// r) {
	// if (order != null && !order.isEmpty()) {
	// r.append(" ORDER BY ");
	// for (int i = 0; i < order.size(); i++) {
	// if (i > 0) {
	// r.append(", ");
	// }
	// Order o = order.get(i);
	// String columnName =
	// emc.getAllPropsMap().get(o.getPropName()).getColumnName();
	//
	// r.append(columnName);
	// r.append(o.getOrder());
	// }
	// }
	// }

	public IQueryObject queryForSelectById(EntityConfig<?> emc, Object id) {
		QueryObject r = new QueryObject();
		r.append("SELECT ");
		{
			int i = 0;
			for (PropertyConfig p : emc.getAllPropsMap().values()) {
				if (i > 0) {
					r.append(",");
				}
				i++;
				r.append(p.getColumnName());
			}
		}
		r.append(" FROM ");
		r.append(emc.getTableName());

		r.append(" WHERE ");

		{
			int i = 0;
			for (PropertyConfig p : emc.getIdProps()) {
				if (i > 0) {
					r.append(" AND ");
				}
				i++;
				r.append(p.getColumnName());
				r.append("=?");
				r.addArg(p.getJdbcValue(id, 1));
			}
		}
		return r;
	}

	public IQueryObject queryForUpdate(EntityConfig<?> emc, Object entity) {
		QueryObject r = new QueryObject();
		r.append("UPDATE ");
		r.append(emc.getTableName());
		r.append(" SET ");
		{
			int i = 0;
			for (PropertyConfig p : emc.getRegularProps()) {
				if (i > 0) {
					r.append(", ");
				}
				i++;
				r.append(p.getColumnName());
				r.append("=?");
				r.addArg(p.getJdbcValue(entity));
			}
		}
		r.append(" WHERE ");
		{
			int i = 0;
			for (PropertyConfig p : emc.getIdProps()) {
				if (i > 0) {
					r.append(" AND ");
				}
				i++;
				r.append(p.getColumnName());
				r.append("=?");
				r.addArg(p.getJdbcValue(entity));
			}
		}
		return r;
	}

	public IQueryObject queryForInsert(EntityConfig<?> emc, Object entity) {
		QueryObject r = new QueryObject();
		r.append("INSERT INTO ");
		r.append(emc.getTableName());
		r.append(" (");
		{
			int i = 0;
			for (PropertyConfig p : emc.getAllPropsMap().values()) {
				if (i > 0) {
					r.append(", ");
				}
				i++;
				r.append(p.getColumnName());
			}
		}
		r.append(") VALUES (");
		{
			int i = 0;
			for (PropertyConfig p : emc.getAllPropsMap().values()) {
				if (i > 0) {
					r.append(", ");
				}
				i++;
				r.append("?");
				r.addArg(p.getJdbcValue(entity));
			}
		}
		r.append(")");
		return r;
	}

	public IQueryObject queryForDelete(EntityConfig<?> emc, Object entity) {
		QueryObject r = new QueryObject();
		r.append("DELETE FROM ");
		r.append(emc.getTableName());
		r.append(" WHERE ");
		{
			int i = 0;
			for (PropertyConfig p : emc.getIdProps()) {
				if (i > 0) {
					r.append(" AND ");
				}
				i++;
				r.append(p.getColumnName());
				r.append("=?");
				r.addArg(p.getJdbcValue(entity));
			}
		}
		return r;
	}

	public IQueryObject queryForDeleteById(EntityConfig<?> entityConfig, Object id) {
		QueryObject q = new QueryObject();
		q.append("DELETE FROM ");
		q.append(entityConfig.getTableName());
		q.append(" WHERE ");
		{
			int c = 0;
			for (PropertyConfig p : entityConfig.getIdProps()) {
				if (c > 0) {
					q.append(" AND ");
				}
				q.append(p.getColumnName());
				q.append("=?");
				q.addArg(p.getJdbcValue(id, 1));
				c++;
			}
		}
		return q;
	}

	public IQueryObject refresh(EntityConfig<?> entityConfig, Object entity) {
		QueryObject q = new QueryObject();
		q.append("select ");
		{
			int c = 0;
			for (PropertyConfig p : entityConfig.getAllPropsMap().values()) {
				if (c > 0) {
					q.append(",");
				}
				q.append(p.getColumnName());
				c++;
			}
		}
		q.append(" from ");
		q.append(entityConfig.getTableName());
		q.append(" where ");
		{
			int c = 0;
			for (PropertyConfig p : entityConfig.getIdProps()) {
				if (c > 0) {
					q.append(" and ");
				}
				q.append(p.getColumnName());
				q.append("=?");
				q.addArg(p.getJdbcValue(entity));
				c++;
			}
		}
		return q;
	}

	public IQueryObject existsById(EntityConfig<?> entityConfig, Object id) {
		QueryObject q = new QueryObject();
		q.append("SELECT COUNT(*) FROM ");
		q.append(entityConfig.getTableName());
		q.append(" WHERE ");
		{
			int c = 0;
			for (PropertyConfig p : entityConfig.getIdProps()) {
				if (c > 0) {
					q.append(" AND ");
				}
				q.append(p.getColumnName());
				q.append("=?");
				q.addArg(p.getJdbcValue(id, 1));
				c++;
			}
		}
		return q;
	}

	public IQueryObject exists(EntityConfig<?> entityConfig, Object entity) {
		QueryObject q = new QueryObject();
		q.append("SELECT COUNT(*) FROM ");
		q.append(entityConfig.getTableName());
		q.append(" WHERE ");
		{
			int c = 0;
			for (PropertyConfig p : entityConfig.getIdProps()) {
				if (c > 0) {
					q.append(" AND ");
				}
				q.append(p.getColumnName());
				q.append("=?");
				q.addArg(p.getJdbcValue(entity));
				c++;
			}
		}
		return q;
	}

	public String sortBy(EntityConfig<?> entityConfig, List<Order> orders) {
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
