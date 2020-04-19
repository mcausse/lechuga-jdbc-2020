package org.lechuga.examples;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.Before;
import org.junit.Test;
import org.lechuga.EntityListener;
import org.lechuga.EntityManager;
import org.lechuga.EntityManagerFactory;
import org.lechuga.anno.EntityListeners;
import org.lechuga.anno.Generated;
import org.lechuga.anno.Id;
import org.lechuga.anno.Table;
import org.lechuga.autogen.impl.HsqldbIdentity;
import org.lenteja.jdbc.DataAccesFacade;
import org.lenteja.jdbc.JdbcDataAccesFacade;
import org.lenteja.jdbc.script.SqlScriptExecutor;

public class PizzaEntityManagerTest {

	final DataAccesFacade facade;

	public PizzaEntityManagerTest() {
		final JDBCDataSource ds = new JDBCDataSource();
		ds.setUrl("jdbc:hsqldb:mem:pizza");
		ds.setUser("sa");
		ds.setPassword("");
		this.facade = new JdbcDataAccesFacade(ds);
	}

	@Before
	public void before() {
		facade.begin();
		try {
			SqlScriptExecutor sql = new SqlScriptExecutor(facade);
			sql.runFromClasspath("pizza_exps.sql");
			facade.commit();
		} catch (Throwable e) {
			facade.rollback();
			throw e;
		}
	}

	@EntityListeners({ PizzaEntityListener.class })
	@Table("pizzas")
	public static class Pizza {

		@Id
		@Generated(HsqldbIdentity.class)
		public Long idPizza;

		public String name;

		public Double price;

		public Pizza() {
			super();
		}

		public Pizza(String name, Double price) {
			super();
			this.name = name;
			this.price = price;
		}

		@Override
		public String toString() {
			return "Pizza [idPizza=" + idPizza + ", name=" + name + ", price=" + price + "]";
		}

	}

	public static class PizzaEntityListener extends EntityListener<Pizza> {
		@Override
		public void afterStore(EntityManager em, Pizza entity) {
			System.out.println("storation!");
		}
	}

	@Test
	public void testName() throws Exception {

		EntityManager em = new EntityManagerFactory().buildEntityManager(facade, Pizza.class);

		facade.begin();
		try {
			{
				Pizza romana = new Pizza("romana", 12.5);
				Pizza napolitana = new Pizza("napolitana", 10.25);
				assertEquals("Pizza [idPizza=null, name=romana, price=12.5]", romana.toString());
				assertEquals("Pizza [idPizza=null, name=napolitana, price=10.25]", napolitana.toString());

				em.store(romana);
				em.store(napolitana);
				assertEquals("Pizza [idPizza=10, name=romana, price=12.5]", romana.toString());
				assertEquals("Pizza [idPizza=11, name=napolitana, price=10.25]", napolitana.toString());

				assertEquals(
						"[Pizza [idPizza=10, name=romana, price=12.5], Pizza [idPizza=11, name=napolitana, price=10.25]]",
						em.loadAll(Pizza.class).toString());
			}
			{
				Pizza romana1 = em.loadById(Pizza.class, 10L);
				Pizza romana2 = em.loadById(Pizza.class, 10L);

				romana1.price = 13.0;
				em.store(romana1);
				em.refresh(romana2);

				assertEquals("Pizza [idPizza=10, name=romana, price=13.0]", romana1.toString());
				assertEquals("Pizza [idPizza=10, name=romana, price=13.0]", romana1.toString());
			}
			{
				List<Pizza> r = em.buildQuery() //
						.addAlias("p", Pizza.class) //
						.append("select {p.*} from {p.#}") //
						.append(" where {p.price between ? and ?}", 10.0, 20.0) //
						.append(" order by {p.price asc}") //
						.getExecutor(Pizza.class) //
						.load() //
				;
				assertEquals("[Pizza [idPizza=11, name=napolitana, price=10.25], "
						+ "Pizza [idPizza=10, name=romana, price=13.0]]", r.toString());
			}
			{
				Pizza romana = em.loadById(Pizza.class, 10L);

				assertTrue(em.exists(romana));
				assertTrue(em.existsById(Pizza.class, 11L));

				em.delete(romana);
				em.deleteById(Pizza.class, 11L);

				assertFalse(em.exists(romana));
				assertFalse(em.existsById(Pizza.class, 11L));

				assertEquals("[]", em.loadAll(Pizza.class).toString());
			}

			facade.commit();
		} catch (Exception e) {
			facade.rollback();
			throw e;
		}
	}

}
