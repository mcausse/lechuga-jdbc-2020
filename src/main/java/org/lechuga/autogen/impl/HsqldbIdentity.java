package org.lechuga.autogen.impl;

import org.lechuga.autogen.AbstractGenerator;
import org.lenteja.jdbc.query.QueryObject;

public class HsqldbIdentity extends AbstractGenerator {

	public HsqldbIdentity() {
		super(new QueryObject("call identity()"), false);
	}
}