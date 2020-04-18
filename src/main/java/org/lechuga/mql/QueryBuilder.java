package org.lechuga.mql;

import java.util.LinkedHashMap;
import java.util.Map;

import org.lechuga.EntityConfig;
import org.lechuga.EntityManager;
import org.lechuga.mql.typesafe.Predicates;
import org.lenteja.jdbc.Mapable;
import org.lenteja.jdbc.query.IQueryObject;
import org.lenteja.jdbc.query.QueryObject;

public class QueryBuilder {

	private final QueryFormatter queryFormatter;

	private final EntityManager em;

	private final Map<String, EntityConfig<?>> aliases;
	private final QueryObject qo;

	public QueryBuilder(EntityManager em, QueryFormatter queryFormatter) {
		super();
		this.em = em;
		this.queryFormatter = queryFormatter;
		this.aliases = new LinkedHashMap<>();
		this.qo = new QueryObject();
	}

	public QueryBuilder(EntityManager em) {
		this(em, new DefaultQueryFormatter2020());
	}

	public QueryBuilder addAlias(String alias, Class<?> entityClass) {
		aliases.put(alias, em.getEntityConfigFor(entityClass));
		return this;
	}

	// TODO rename
	public <E> Predicates<E> addAliasAndBuildPredicates(String alias, Class<E> entityClass) {
		addAlias(alias, entityClass);
		return new Predicates<>(em.getEntityConfigFor(entityClass), alias);
	}

	// =========================================================
	// =========================================================
	// =========================================================

	public QueryBuilder append(IQueryObject qo) {
		this.qo.append(queryFormatter.format(aliases, qo.getQuery(), qo.getArgs()));
		return this;
	}

	public QueryBuilder append(String queryFragment, Object... args) {
		this.qo.append(queryFormatter.format(aliases, queryFragment, args));
		return this;
	}

	public IQueryObject getQueryObject() {
		return qo;
	}

	public <T> Executor<T> getExecutor(Class<T> entityClassResult) {
		EntityConfig<T> entityMeta = em.getEntityConfigFor(entityClassResult);
		return getExecutor(entityMeta);
	}

	public <T> Executor<T> getExecutor(Mapable<T> mapable) {
		return new Executor<T>(em.getFacade(), qo, mapable);
	}

	@Override
	public String toString() {
		return qo.toString();
	}

}
