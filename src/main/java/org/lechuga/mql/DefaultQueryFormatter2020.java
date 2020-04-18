package org.lechuga.mql;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.lechuga.EntityConfig;
import org.lechuga.PropertyConfig;
import org.lenteja.jdbc.query.IQueryObject;
import org.lenteja.jdbc.query.QueryObject;

/**
 * The Martin-QL Expression Processor
 *
 * <pre>
 *	testStar:
 *	   Expression: {p.*} -- []
 *	   Result:     p.id_pizza,p.name,p.price -- []
 *	testCondition:
 *	   Expression: {p.name=?} -- [chucho]
 *	   Result:     p.name=? -- [chucho(String)]
 *	testTableName:
 *	   Expression: {p.#} -- []
 *	   Result:     pizzas p -- []
 *	testField:
 *	   Expression: {p.name} -- []
 *	   Result:     p.name -- []
 *	testIQueryObject:
 *	   Expression: {} -- [abc=? -- [3(Long)]]
 *	   Result:     abc=? -- [3(Long)]
 * </pre>
 *
 * @author mohms
 */
public class DefaultQueryFormatter2020 implements QueryFormatter {

	protected class ExactConsumer<T> {

		final Iterator<T> iterator;

		public ExactConsumer(Collection<T> c) {
			super();
			this.iterator = c.iterator();
		}

		@SuppressWarnings("unchecked")
		public <K> K next(Class<K> type) {
			if (!iterator.hasNext()) {
				throw new RuntimeException("no more args");
			}
			Object r = iterator.next();
			if (!type.isAssignableFrom(r.getClass())) {
				throw new RuntimeException("expected an argument of type '" + type.getName() + ", but obtained: " + r
						+ " (of type " + r.getClass() + ")");
			}
			return (K) r;
		}

		public void verifyAllConsumed() {
			if (iterator.hasNext()) {
				throw new RuntimeException("not all args are consumed");
			}
		}
	}

	@Override
	public IQueryObject format(Map<String, EntityConfig<?>> aliases, String fragment, Object[] args) {
		QueryObject qo = new QueryObject();
		ExactConsumer<Object> c = new ExactConsumer<>(Arrays.asList(args));

		try {

			int index = 0;
			while (index >= 0) {

				int indexBeginExp = fragment.indexOf('{', index);
				if (indexBeginExp >= 0) {
					qo.append(fragment.substring(index, indexBeginExp));
				} else {
					qo.append(fragment.substring(index));
					break;
				}
				int indexEndExp = fragment.indexOf('}', indexBeginExp + 1);
				if (indexEndExp >= 0) {
					String exp = fragment.substring(indexBeginExp + 1, indexEndExp);
					processExpression(aliases, exp.trim(), c, qo);
					index = indexEndExp + 1;
				} else {
					throw new RuntimeException("expected } but end reached");
				}
			}

			c.verifyAllConsumed();
			return qo;
		} catch (Exception e) {
			throw new RuntimeException("error with query format: '" + fragment + " -- " + Arrays.toString(args) + "'",
					e);
		}
	}

	protected void processExpression(Map<String, EntityConfig<?>> aliases, String exp, ExactConsumer<Object> c,
			QueryObject qo) {

		if (exp.isEmpty()) {
			IQueryObject q = c.next(IQueryObject.class);
			qo.append(q);
		} else {
			int i = 0;

			// parse alias
			final String alias;
			{
				while (Character.isJavaIdentifierPart(exp.charAt(i))) {
					i++;
				}
				alias = exp.substring(0, i);
				if (exp.charAt(i) != '.') {
					throw new RuntimeException("expected a '.' after alias '" + alias + "'");
				}
				i++; // chupa .
			}

			// parse prop name
			final String propName;
			{
				int startPropName = i;
				while (i < exp.length() && (Character.isJavaIdentifierPart(exp.charAt(i)) || exp.charAt(i) == '.'
						|| exp.charAt(i) == '*' || exp.charAt(i) == '#')) {
					i++;
				}
				propName = exp.substring(startPropName, i);
			}
			final String rest = exp.substring(i);

			/////

			if (!aliases.containsKey(alias)) {
				throw new RuntimeException("alias not found: '" + alias + "', valid are: " + aliases.keySet());
			}
			EntityConfig<?> ec = aliases.get(alias);

			switch (propName) {
			case "*":
				int k = 0;
				for (PropertyConfig prop : ec.getAllPropsMap().values()) {
					if (k > 0) {
						qo.append(",");
					}
					qo.append(alias);
					qo.append(".");
					qo.append(prop.getColumnName());
					k++;
				}
				break;
			case "#":
				qo.append(ec.getTableName());
				qo.append(" ");
				qo.append(alias);
				break;
			default:
				qo.append(alias);
				qo.append(".");
				if (!ec.getAllPropsMap().containsKey(propName)) {
					throw new RuntimeException(
							"property not defined: '" + ec.getEntityClass().getName() + "#" + propName);
				}
				PropertyConfig prop = ec.getAllPropsMap().get(propName);
				qo.append(prop.getColumnName());
				qo.append(rest);
				int kk = rest.indexOf('?');
				while (kk >= 0) {
					Object arg = c.next(Object.class);
					arg = prop.getColumnHandler().getJdbcValue(arg);
					qo.addArg(arg);
					kk = rest.indexOf('?', kk + 1);
				}
			}

		}

	}

}
