package org.lechuga.autogen.impl;

import org.lechuga.autogen.AbstractGenerator;
import org.lenteja.jdbc.query.QueryObject;

public class MySqlIdentity extends AbstractGenerator {

	public MySqlIdentity() {
		super(new QueryObject("select last_insert_id()"), false);
	}
}
