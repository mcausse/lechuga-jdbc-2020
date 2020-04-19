package org.lechuga;

import java.util.List;
import java.util.Map;

import org.lechuga.autogen.Generator;
import org.lechuga.handler.ScalarMappers;
import org.lechuga.mql.QueryBuilder;
import org.lenteja.jdbc.DataAccesFacade;
import org.lenteja.jdbc.Mapable;
import org.lenteja.jdbc.exception.EmptyResultException;
import org.lenteja.jdbc.exception.JdbcException;
import org.lenteja.jdbc.exception.TooManyResultsException;
import org.lenteja.jdbc.exception.UnexpectedResultException;
import org.lenteja.jdbc.query.IQueryObject;
import org.lenteja.jdbc.query.QueryObject;

public class EntityManager {

	final Map<Class<?>, EntityConfig<?>> entityConfigs;
	final DataAccesFacade facade;

	public EntityManager(Map<Class<?>, EntityConfig<?>> entityConfigs, DataAccesFacade facade) {
		super();
		this.entityConfigs = entityConfigs;
		this.facade = facade;
	}

	public DataAccesFacade getFacade() {
		return facade;
	}

	@SuppressWarnings("unchecked")
	public <E> EntityConfig<E> getEntityConfigFor(Class<E> entityClass) {
		if (!entityConfigs.containsKey(entityClass)) {
			throw new RuntimeException("entity not defined: " + entityClass.getName() + "; valid ones="
					+ entityConfigs.keySet().toString());
		}
		return (EntityConfig<E>) entityConfigs.get(entityClass);
	}

	public QueryBuilder buildQuery() {
		return new QueryBuilder(this);
	}

	public <E> List<E> loadAll(Class<E> entityClass) {
		return loadAll(entityClass, null);
	}

	public <E> List<E> loadAll(Class<E> entityClass, List<Order> orders) {
		EntityConfig<E> entityConfig = getEntityConfigFor(entityClass);
		QueryObject q = entityConfig.getEntityManagerOperations().selectAll();
		q.append(entityConfig.getEntityManagerOperations().sortBy(orders));
		List<E> r = facade.load(q, entityConfig);
		r.forEach(e -> entityConfig.getListeners().forEach(l -> l.afterLoad(this, e)));
		return r;
	}

	public <E, ID> E loadById(Class<E> entityClass, ID id) throws TooManyResultsException, EmptyResultException {
		EntityConfig<E> entityConfig = getEntityConfigFor(entityClass);
		IQueryObject q = entityConfig.getEntityManagerOperations().selectById(id);
		E r = facade.loadUnique(q, entityConfig);
		entityConfig.getListeners().forEach(l -> l.afterLoad(this, r));
		return r;
	}

	@SuppressWarnings("unchecked")
	public <E> void refresh(E entity) throws TooManyResultsException, EmptyResultException {
		Class<E> entityClass = (Class<E>) entity.getClass();
		EntityConfig<E> entityConfig = getEntityConfigFor(entityClass);
		IQueryObject q = entityConfig.getEntityManagerOperations().refresh(entity);
		E e = facade.loadUnique(q, entityConfig);

		for (PropertyConfig p : entityConfig.getAllPropsMap().values()) {
			Object value = p.getValue(e);
			p.setValue(entity, value);
		}
		entityConfig.getListeners().forEach(l -> l.afterLoad(this, e));
	}

	@SuppressWarnings("unchecked")
	public <E> void update(E entity) throws UnexpectedResultException {

		Class<E> entityClass = (Class<E>) entity.getClass();

		EntityConfig<E> entityConfig = getEntityConfigFor(entityClass);

		entityConfig.getListeners().forEach(l -> l.beforeUpdate(this, entity));
		entityConfig.getListeners().forEach(l -> l.beforeStore(this, entity));

		IQueryObject q = entityConfig.getEntityManagerOperations().update(entity);
		int affectedRows = facade.update(q);
		if (affectedRows != 1) {
			throw new UnexpectedResultException(
					"expected affectedRows=1, but affectedRows=" + affectedRows + " for " + q);
		}

		entityConfig.getListeners().forEach(l -> l.afterUpdate(this, entity));
		entityConfig.getListeners().forEach(l -> l.afterStore(this, entity));
	}

	@SuppressWarnings("unchecked")
	public <E> void insert(E entity) {

		Class<E> entityClass = (Class<E>) entity.getClass();
		EntityConfig<E> entityConfig = getEntityConfigFor(entityClass);

		entityConfig.getListeners().forEach(l -> l.beforeInsert(this, entity));
		entityConfig.getListeners().forEach(l -> l.beforeStore(this, entity));

		for (PropertyConfig autoGenProp : entityConfig.getAutogenProps()) {
			Generator ag = autoGenProp.getGenerator();
			if (ag.isBeforeInsert()) {
				final Object autoGeneratedVal = autogeneratePrimaryKeyValue(entity, autoGenProp, ag);
				autoGenProp.setValue(entity, autoGeneratedVal);
			}
		}

		IQueryObject q = entityConfig.getEntityManagerOperations().insert(entity);
		facade.update(q);

		for (PropertyConfig autoGenProp : entityConfig.getAutogenProps()) {
			Generator ag = autoGenProp.getGenerator();
			if (!ag.isBeforeInsert()) {
				final Object autoGeneratedVal = autogeneratePrimaryKeyValue(entity, autoGenProp, ag);
				autoGenProp.setValue(entity, autoGeneratedVal);
			}
		}

		entityConfig.getListeners().forEach(l -> l.afterInsert(this, entity));
		entityConfig.getListeners().forEach(l -> l.afterStore(this, entity));
	}

	protected <E> Object autogeneratePrimaryKeyValue(E entity, PropertyConfig autoGenProp, Generator ag) {
		if (autoGenProp.getValue(entity) != null) {
			throw new RuntimeException(
					"autogen must be null for insert(): " + autoGenProp.toString() + ": entity=" + entity.toString());
		}
		Mapable<Object> scalarMapper = ScalarMappers.getScalarMapperFor(autoGenProp.getPropertyType());
		final Object autoGeneratedVal = facade.loadUnique(ag.getQuery(), scalarMapper);
		return autoGeneratedVal;
	}

	/**
	 * fa un {@link #insert(Object)} o un {@link #update(Object)}, segons convingui.
	 *
	 * <pre>
	 * si almenys una PK és Autogen:
	 *         si alguna PK no-Autogen té valor null => error
	 *         insert: alguna PK val null
	 *         update: cap PK val null
	 * sino
	 *         si almenys una PK està a null =&gt; error
	 *
	 *         si exist()
	 *             update()
	 *         sino
	 *             insert()
	 *         fisi
	 * fisi
	 * </pre>
	 *
	 * @throws UnexpectedResultException
	 */
	@SuppressWarnings("unchecked")
	public <E> void store(E entity) throws UnexpectedResultException {

		Class<E> entityClass = (Class<E>) entity.getClass();

		EntityConfig<E> entityConfig = getEntityConfigFor(entityClass);

		/**
		 * si una propietat PK és primitiva, el seu valor mai serà null (p.ex. serà 0) i
		 * l'store() no funcionarà. Si es té una PK primitiva, usar insert()/update() en
		 * comptes d'store().
		 */

		for (PropertyConfig p : entityConfig.getIdProps()) {
			if (p.getPropertyType().isPrimitive()) {
				throw new JdbcException(
						"PK column is mapped as of primitive type: use insert()/update() instead of store(): "
								+ entity.getClass().getSimpleName() + "#" + p.getPropertyName());
			}
		}

		boolean algunaPkAutogen = !entityConfig.getAutogenProps().isEmpty();
		if (algunaPkAutogen) {

			for (PropertyConfig p : entityConfig.getIdProps()) {
				if (p.getGenerator() == null) {
					if (p.getValue(entity) == null) {
						throw new JdbcException("una propietat PK no-autogenerada té valor null en store(): "
								+ entity.getClass().getSimpleName() + "#" + p.getPropertyName());
					}
				}
			}

			boolean algunaPkValNull = false;
			for (PropertyConfig p : entityConfig.getIdProps()) {
				if (p.getValue(entity) == null) {
					algunaPkValNull = true;
					break;
				}
			}

			if (algunaPkValNull) {
				insert(entity);
			} else {
				update(entity);
			}

		} else {

			/**
			 * <pre>
			 *         si almenys una PK està a null =&gt; error
			 *
			 *         si exist()
			 *             update()
			 *         sino
			 *             insert()
			 *         fisi
			 * </pre>
			 */
			for (PropertyConfig p : entityConfig.getIdProps()) {
				if (p.getValue(entity) == null) {
					throw new JdbcException("una propietat PK no-autogenerada té valor null en store(): "
							+ entity.getClass().getSimpleName() + "#" + p.getPropertyName());
				}
			}

			if (exists(entity)) {
				update(entity);
			} else {
				insert(entity);
			}

		}
	}

	@SuppressWarnings("unchecked")
	public <E> void delete(E entity) throws UnexpectedResultException {

		Class<E> entityClass = (Class<E>) entity.getClass();
		EntityConfig<E> entityConfig = getEntityConfigFor(entityClass);

		entityConfig.getListeners().forEach(l -> l.beforeDelete(this, entity));

		IQueryObject q = entityConfig.getEntityManagerOperations().delete(entity);
		int affectedRows = facade.update(q);
		if (affectedRows != 1) {
			throw new UnexpectedResultException(
					"expected affectedRows=1, but affectedRows=" + affectedRows + " for " + q);
		}

		entityConfig.getListeners().forEach(l -> l.afterDelete(this, entity));
	}

	public <E, ID> void deleteById(Class<E> entityClass, ID id) throws TooManyResultsException, EmptyResultException {
		EntityConfig<E> entityConfig = getEntityConfigFor(entityClass);

		final E e;
		if (entityConfig.getListeners().isEmpty()) {
			e = null;
		} else {
			e = loadById(entityClass, id);
			entityConfig.getListeners().forEach(l -> l.beforeDelete(this, e));
		}

		IQueryObject q = entityConfig.getEntityManagerOperations().deleteById(id);
		int affectedRows = facade.update(q);
		if (affectedRows != 1) {
			throw new UnexpectedResultException(
					"expected affectedRows=1, but affectedRows=" + affectedRows + " for " + q);
		}

		entityConfig.getListeners().forEach(l -> l.afterDelete(this, e));
	}

	public <E, ID> boolean existsById(Class<E> entityClass, ID id) {

		EntityConfig<E> entityConfig = getEntityConfigFor(entityClass);

		IQueryObject q = entityConfig.getEntityManagerOperations().existsById(id);
		long rows = facade.loadUnique(q, ScalarMappers.LONG);
		return rows > 0L;
	}

	@SuppressWarnings("unchecked")
	public <E> boolean exists(E entity) {

		Class<E> entityClass = (Class<E>) entity.getClass();
		EntityConfig<E> entityConfig = getEntityConfigFor(entityClass);

		IQueryObject q = entityConfig.getEntityManagerOperations().exists(entity);
		long rows = facade.loadUnique(q, ScalarMappers.LONG);
		return rows > 0L;
	}

	// ===========================================================

	@SuppressWarnings("unchecked")
	public <E> E loadUniqueByExample(E example) throws TooManyResultsException, EmptyResultException {

		Class<E> entityClass = (Class<E>) example.getClass();
		EntityConfig<E> entityConfig = getEntityConfigFor(entityClass);

		QueryObject q = entityConfig.getEntityManagerOperations().selectAll();
		q.append(" where 1=1");
		for (PropertyConfig p : entityConfig.getAllPropsMap().values()) {
			Object v = p.getValue(example);
			if (v != null) {
				q.append(" and ");
				q.append(p.getColumnName());
				q.append("=?");
				q.addArg(p.getJdbcValue(example));
			}
		}
		E r = getFacade().loadUnique(q, entityConfig);
		entityConfig.getListeners().forEach(l -> l.afterLoad(this, r));
		return r;
	}

	@SuppressWarnings("unchecked")
	public <E> List<E> loadByExample(E example, List<Order> orders) {

		Class<E> entityClass = (Class<E>) example.getClass();
		EntityConfig<E> entityConfig = getEntityConfigFor(entityClass);

		QueryObject q = entityConfig.getEntityManagerOperations().selectAll();

		/*
		 * where specs
		 */
		q.append(" where 1=1");
		for (PropertyConfig p : entityConfig.getAllPropsMap().values()) {
			Object v = p.getValue(example);
			if (v != null) {
				q.append(" and ");
				q.append(p.getColumnName());
				q.append("=?");
				q.addArg(p.getJdbcValue(example));
			}
		}

		q.append(entityConfig.getEntityManagerOperations().sortBy(orders));

		List<E> r = getFacade().load(q, entityConfig);
		r.forEach(e -> entityConfig.getListeners().forEach(l -> l.afterLoad(this, e)));
		return r;
	}

	// TODO que fer amb aixo?

	// public <E> List<E> loadBy(Class<E> entityClass, Restriction restrinction,
	// List<Order> orders) {
	//
	// EntityConfig<E> entityConfig = getEntityConfig(entityClass);
	//
	// QueryObject q = ops.queryForSelectAll(entityConfig);
	//
	// /*
	// * where specs
	// */
	// q.append(" where 1=1");
	// for (PropertyConfig p : entityConfig.getAllPropsMap().values()) {
	// Object v = p.getValue(example);
	// if (v != null) {
	// q.append(" and ");
	// q.append(p.getColumnName());
	// q.append("=?");
	// q.addArg(p.getJdbcValue(example));
	// }
	// }
	//
	// q.append(ops.sortBy(entityConfig, orders));
	//
	// return getFacade().load(q, entityConfig);
	// }

	// ===========================================================

	// @FunctionalInterface
	// public static interface Spec {
	// void apply(QueryBuilder q);
	// }
	//
	// public <E> List<E> loadBy(Class<E> entityClass, Spec spec) {
	// return loadBy(entityClass, spec, null);
	// }
	//
	// public <E> List<E> loadBy(Class<E> entityClass, Spec spec, List<Order>
	// orders) {
	//
	// QueryBuilder qb = buildQuery();
	// qb.addAlias("model", entityClass);
	// qb.append("select {model.*} from {model.#} where ");
	// spec.apply(qb);
	//
	// /*
	// * order by
	// */
	// if (orders != null && !orders.isEmpty()) {
	// qb.append(" order by ");
	// int i = 0;
	// for (Order o : orders) {
	// if (i > 0) {
	// qb.append(", ");
	// i++;
	// }
	// qb.append("{" + o.getPropName() + "} " + o.getOrder());
	// }
	// }
	//
	// return qb.getExecutor(entityClass).load();
	// }
	//
	// public <E> E loadUniqueBy(Class<E> entityClass, Spec spec) {
	//
	// QueryBuilder qb = buildQuery();
	// qb.addAlias("model", entityClass);
	// qb.append("select {model.*} from {model.#} where ");
	// spec.apply(qb);
	//
	// return qb.getExecutor(entityClass).loadUnique();
	// }

}