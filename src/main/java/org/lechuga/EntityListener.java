package org.lechuga;

public abstract class EntityListener<E> {

	public void afterLoad(EntityManager em, E entity) {
	}

	public void beforeStore(EntityManager em, E entity) {
	}

	public void afterStore(EntityManager em, E entity) {
	}

	public void beforeInsert(EntityManager em, E entity) {
	}

	public void afterInsert(EntityManager em, E entity) {
	}

	public void beforeUpdate(EntityManager em, E entity) {
	}

	public void afterUpdate(EntityManager em, E entity) {
	}

	public void beforeDelete(EntityManager em, E entity) {
	}

	public void afterDelete(EntityManager em, E entity) {
	}

}