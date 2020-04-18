package org.lechuga.autogen.impl;

import org.lechuga.autogen.AbstractGenerator;
import org.lenteja.jdbc.query.QueryObject;

public class HsqldbSequence extends AbstractGenerator {

	public HsqldbSequence(String sequenceName) {
		super(new QueryObject("call next value for " + sequenceName), true);
	}
}
