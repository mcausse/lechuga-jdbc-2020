package org.lenteja.jdbc.query;

public interface QueryObjectUtils {

	public static final int QUERY_STRING_LIMIT = 2000;

	public static String toString(IQueryObject q) {
		return toString(q.getQuery(), q.getArgs());
	}

	public static String toString(String query, Object[] args) {

		final StringBuilder r = new StringBuilder();

		int index = Math.min(QUERY_STRING_LIMIT, query.length());
		query = query.substring(0, index);

		r.append(query);
		r.append(" -- [");
		int c = 0;
		for (final Object o : args) {
			if (c > 0) {
				r.append(", ");
			}
			r.append(o);
			if (o != null) {
				r.append("(");
				r.append(o.getClass().getSimpleName());
				r.append(")");
			}
			c++;
		}
		r.append("]");
		return r.toString();
	}

}
