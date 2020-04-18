package org.lechuga.mql;

import java.util.Map;

import org.lechuga.EntityConfig;
import org.lenteja.jdbc.query.IQueryObject;

public interface QueryFormatter {

	IQueryObject format(Map<String, EntityConfig<?>> aliases, String fragment, Object[] args);

}
