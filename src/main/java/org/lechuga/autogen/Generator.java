package org.lechuga.autogen;

import org.lenteja.jdbc.query.IQueryObject;

public interface Generator {

	boolean isBeforeInsert();

	IQueryObject getQuery();

}