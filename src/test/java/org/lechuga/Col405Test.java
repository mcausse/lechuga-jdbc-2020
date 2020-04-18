package org.lechuga;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.Before;
import org.junit.Test;
import org.lechuga.anno.Column;
import org.lechuga.anno.Id;
import org.lechuga.anno.Table;
import org.lenteja.jdbc.DataAccesFacade;
import org.lenteja.jdbc.JdbcDataAccesFacade;
import org.lenteja.jdbc.query.QueryObject;

public class Col405Test {

	final DataAccesFacade facade;

	public Col405Test() {
		final JDBCDataSource ds = new JDBCDataSource();
		ds.setUrl("jdbc:hsqldb:mem:col405");
		ds.setUser("sa");
		ds.setPassword("");
		this.facade = new JdbcDataAccesFacade(ds);
	}

	@Before
	public void before() {
		facade.begin();
		try {
			facade.update(new QueryObject("drop table col405 if exists;"));
			facade.update(new QueryObject(
					"create table col405 (k varchar(10) primary key, v varchar(255) not null, active boolean not null);"));
			facade.commit();
		} catch (Throwable e) {
			facade.rollback();
			throw e;
		}
	}

	@Table("col405")
	public static class Col405 {

		@Id
		@Column("k")
		public String key;

		@Column("v")
		public String value;

		@Column("active")
		public Boolean enabled;

		public Col405() {
			super();
		}

		public Col405(String key, String value, Boolean enabled) {
			super();
			this.key = key;
			this.value = value;
			this.enabled = enabled;
		}

		@Override
		public String toString() {
			return String.format("[%s=>%s (%s)]", key, value, enabled);
		}
	}

	@Test
	public void testName() throws Exception {

		EntityManager em = new EntityManagerFactory().buildEntityManager(facade, Col405.class);

		facade.begin();
		try {

			Col405 kv1 = new Col405("1", "one", true);
			Col405 kv2 = new Col405("2", "two", false);
			Col405 kv3 = new Col405("3", "three", true);

			em.store(kv1);
			em.store(kv2);
			em.store(kv3);

			kv1.value = "u";
			kv2.value = "dos";
			kv3.value = "tres";

			em.store(kv1);
			em.store(kv2);
			em.store(kv3);

			List<Col405> all = em.buildQuery().addAlias("c", Col405.class)
					.append("select {c.*} from {c.#} where {c.enabled is true} order by {c.key} asc")
					.getExecutor(Col405.class).load();
			assertEquals("[[1=>u (true)], [3=>tres (true)]]", all.toString());

			facade.commit();
		} catch (Exception e) {
			facade.rollback();
			throw e;
		}
	}

	@Test
	public void testGenericDao() throws Exception {

		EntityManager em = new EntityManagerFactory().buildEntityManager(facade, Col405.class);
		GenericDao<Col405, String> dao = new GenericDao<>(em, Col405.class);

		facade.begin();
		try {

			Col405 kv1 = new Col405("1", "one", true);
			Col405 kv2 = new Col405("2", "two", false);
			Col405 kv3 = new Col405("3", "three", true);

			dao.insertAll(Arrays.asList(kv1, kv2, kv3));

			kv1.value = "u";
			kv2.value = "dos";
			kv3.value = "tres";

			dao.updateAll(Arrays.asList(kv1, kv2, kv3));

			{
				Col405 probe = new Col405();
				probe.enabled = true;
				List<Col405> all = dao.loadByExample(probe, Order.by(Order.asc("key")));
				assertEquals("[[1=>u (true)], [3=>tres (true)]]", all.toString());
			}
			{
				Col405 probe = new Col405();
				probe.enabled = false;
				Col405 unique = dao.loadUniqueByExample(probe);
				assertEquals("[2=>dos (false)]", unique.toString());
			}

			assertTrue(dao.exists(kv1));
			assertTrue(dao.existsById(kv2.key));
			dao.delete(kv1);
			dao.deleteById(kv2.key);
			assertFalse(dao.exists(kv1));
			assertFalse(dao.existsById(kv2.key));

			facade.commit();
		} catch (Exception e) {
			facade.rollback();
			throw e;
		}

	}

}
