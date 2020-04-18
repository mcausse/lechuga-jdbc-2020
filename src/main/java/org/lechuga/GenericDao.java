package org.lechuga;

import java.lang.reflect.ParameterizedType;
import java.util.List;

import org.lechuga.mql.QueryBuilder;
import org.lenteja.jdbc.exception.EmptyResultException;
import org.lenteja.jdbc.exception.TooManyResultsException;
import org.lenteja.jdbc.exception.UnexpectedResultException;

public class GenericDao<E, ID> {

	final EntityManager em;
	final Class<E> entityClass;

	public GenericDao(EntityManager em, Class<E> entityClass) {
		super();
		this.em = em;
		this.entityClass = entityClass;
	}

	/**
	 * for derived classes only
	 */
	@SuppressWarnings("unchecked")
	public GenericDao(EntityManager em) {
		super();
		this.em = em;
		this.entityClass = (Class<E>) ((ParameterizedType) getClass().getGenericSuperclass())
				.getActualTypeArguments()[0];
	}

	public EntityManager getEntityManager() {
		return em;
	}

	public Class<E> getEntityClass() {
		return entityClass;
	}

	// ===========================================================
	// ===========================================================
	// ===========================================================

	public QueryBuilder buildQuery() {
		return em.buildQuery();
	}

	// ===========================================================
	// ===========================================================
	// ===========================================================

	public List<E> loadAll() {
		return em.loadAll(entityClass);
	}

	public List<E> loadAll(List<Order> orders) {
		return em.loadAll(entityClass, orders);
	}

	public E loadById(ID id) throws TooManyResultsException, EmptyResultException {
		return em.loadById(entityClass, id);
	}

	public void refresh(E entity) throws TooManyResultsException, EmptyResultException {
		em.refresh(entity);
	}

	public void update(E entity) throws UnexpectedResultException {
		em.update(entity);
	}

	public void insert(E entity) {
		em.insert(entity);
	}

	public void store(E entity) throws UnexpectedResultException {
		em.store(entity);
	}

	public void delete(E entity) throws UnexpectedResultException {
		em.delete(entity);
	}

	public void deleteById(ID id) throws TooManyResultsException, EmptyResultException {
		em.deleteById(entityClass, id);
	}

	public boolean existsById(ID id) {
		return em.existsById(entityClass, id);
	}

	public boolean exists(E entity) {
		return em.exists(entity);
	}

	public E loadUniqueByExample(E example) throws TooManyResultsException, EmptyResultException {
		return em.loadUniqueByExample(example);
	}

	public List<E> loadByExample(E example, List<Order> orders) {
		return em.loadByExample(example, orders);
	}

	// ===========================================================
	// ===========================================================
	// ===========================================================

	public void storeAll(Iterable<E> entities) throws UnexpectedResultException {
		for (E e : entities) {
			store(e);
		}
	}

	public void updateAll(Iterable<E> entities) throws UnexpectedResultException {
		for (E e : entities) {
			update(e);
		}
	}

	public void insertAll(Iterable<E> entities) {
		for (E e : entities) {
			insert(e);
		}
	}

	public void deleteAll(Iterable<E> entities) throws UnexpectedResultException {
		for (E e : entities) {
			delete(e);
		}
	}

	// ===========================================================
	// ===========================================================
	// ===========================================================

}
