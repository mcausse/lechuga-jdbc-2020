package org.lechuga.mql;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;
import org.lechuga.EntityConfig;
import org.lechuga.EntityManagerFactory;
import org.lechuga.anno.Table;
import org.lenteja.jdbc.query.IQueryObject;
import org.lenteja.jdbc.query.QueryObject;

public class DefaultQueryFormatter2020Test {

	final Map<String, EntityConfig<?>> aliases = new LinkedHashMap<>();
	{
		aliases.put("p", new EntityManagerFactory().buildConfig(Pizza.class));
	}

	@Test
	public void testName() throws Exception {

		DefaultQueryFormatter2020 f = new DefaultQueryFormatter2020();

		QueryObject exp = new QueryObject();
		exp.append("abc=?");
		exp.addArg(3L);

		IQueryObject r = f.format(aliases, "{} {p.idPizza} {p.name=?} {p.*} {p.#}", new Object[] { exp, "romana" });
		assertEquals("abc=? p.id_pizza p.name=? p.id_pizza,p.name,p.price pizzas p -- [3(Long), romana(String)]",
				r.toString());
	}

	@Test
	public void testIQueryObject() throws Exception {

		QueryObject exp = new QueryObject();
		exp.append("abc=?");
		exp.addArg(3L);

		evaluate("abc=? -- [3(Long)]", "{}", exp);
	}

	@Test
	public void testStar() throws Exception {

		evaluate("p.id_pizza,p.name,p.price -- []", "{p.*}");
	}

	@Test
	public void testTableName() throws Exception {

		evaluate("pizzas p -- []", "{p.#}");
	}

	@Test
	public void testField() throws Exception {

		evaluate("p.name -- []", "{p.name}");
	}

	@Test
	public void testCondition() throws Exception {

		evaluate("p.name=? -- [chucho(String)]", "{p.name=?}", "chucho");
	}

	private void evaluate(String expected, String format, Object... args) {
		DefaultQueryFormatter2020 f = new DefaultQueryFormatter2020();
		IQueryObject r = f.format(aliases, format, args);
		assertEquals(expected, r.toString());

		final String calleMethod;
		{
			RuntimeException e = new RuntimeException();
			calleMethod = e.getStackTrace()[1].getMethodName();
		}
		System.out.println(calleMethod + ":");
		System.out.println("   Expression: " + format + " -- " + Arrays.toString(args));
		System.out.println("   Result:     " + r);
	}

	@Table("pizzas")
	public static class Pizza {
		public Long idPizza;
		public String name;
		public Double price;
	}
}
