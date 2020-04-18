package org.lechuga.autogen.impl;

import org.lechuga.autogen.AbstractGenerator;
import org.lenteja.jdbc.query.QueryObject;

public class OracleSequence extends AbstractGenerator {

	public OracleSequence(String sequenceName) {
		super(new QueryObject("select " + sequenceName + ".nextval from dual"), true);
	}
}
