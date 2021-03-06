package org.lechuga.mql.typesafe;

import java.util.Arrays;
import java.util.List;

import org.lenteja.jdbc.query.IQueryObject;
import org.lenteja.jdbc.query.QueryObject;

public class Restrictions {

	protected static IQueryObject composition(String op, List<? extends IQueryObject> qs) {
		QueryObject r = new QueryObject();
		for (int i = 0; i < qs.size(); i++) {
			if (i > 0) {
				r.append(op);
			}
			r.append(qs.get(i));
		}
		return r;
	}

	// public static IQueryObject subQueryAliasedAsTable(IQueryObject subquery,
	// String alias) {
	// QueryObject r = new QueryObject();
	// r.append("(");
	// r.append(subquery);
	// r.append(") ");
	// r.append(alias);
	// return r;
	// }

	public static IQueryObject and(List<IQueryObject> qs) {
		return composition(" and ", qs);
	}

	public static IQueryObject or(List<IQueryObject> qs) {
		return composition(" or ", qs);
	}

	public static IQueryObject list(List<IQueryObject> qs) {
		return composition(", ", qs);
	}

	public static IQueryObject and(IQueryObject... qs) {
		return and(Arrays.asList(qs));
	}

	public static IQueryObject or(IQueryObject... qs) {
		return or(Arrays.asList(qs));
	}

	public static IQueryObject list(IQueryObject... qs) {
		return list(Arrays.asList(qs));
	}

	public static IQueryObject not(IQueryObject q) {
		QueryObject r = new QueryObject();
		r.append("not(");
		r.append(q);
		r.append(")");
		return r;
	}

	// public static Object orderBy(List<Order<?>> orders) {
	// IQueryObject r = new IQueryObject();
	// for (Order<?> o : orders) {
	// r.append("not(");
	// o.getPropName()
	// r.append("not(");
	// r.append(q);
	// r.append(")");
	// }
	// return r;
	// }

	// public static IQueryObject all() {
	// return new IQueryObject("1=1");
	// }

}