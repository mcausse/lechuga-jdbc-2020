package org.lechuga.mql;

import java.util.List;

import org.lenteja.jdbc.DataAccesFacade;
import org.lenteja.jdbc.Mapable;
import org.lenteja.jdbc.exception.EmptyResultException;
import org.lenteja.jdbc.exception.TooManyResultsException;
import org.lenteja.jdbc.extractor.PageResult;
import org.lenteja.jdbc.extractor.Pager;
import org.lenteja.jdbc.extractor.ResultSetPagedExtractor;
import org.lenteja.jdbc.query.IQueryObject;

public class Executor<E> {

	final DataAccesFacade facade;
	final IQueryObject qo;
	final Mapable<E> mapable;

	public Executor(DataAccesFacade facade, IQueryObject qo, Mapable<E> mapable) {
		super();
		this.facade = facade;
		this.qo = qo;
		this.mapable = mapable;
	}

	public int update() {
		return facade.update(qo);
	}

	public E loadUnique() throws TooManyResultsException, EmptyResultException {
		return facade.loadUnique(qo, mapable);
	}

	public E loadFirst() throws EmptyResultException {
		return facade.loadFirst(qo, mapable);
	}

	public List<E> load() {
		return facade.load(qo, mapable);
	}

	public PageResult<E> loadPage(Pager<E> pager) {
		Mapable<PageResult<E>> extractor = new ResultSetPagedExtractor<E>(mapable, pager);
		return facade.extract(qo, extractor);
	}

	public IQueryObject getQuery() {
		return qo;
	}

}