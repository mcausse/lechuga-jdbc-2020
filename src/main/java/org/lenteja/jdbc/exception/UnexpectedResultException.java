package org.lenteja.jdbc.exception;

public class UnexpectedResultException extends JdbcException {

	private static final long serialVersionUID = 6193212451419056030L;

	public UnexpectedResultException(String message) {
		super(message);
	}

}
