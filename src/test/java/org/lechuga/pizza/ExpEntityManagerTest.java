package org.lechuga.pizza;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.Before;
import org.junit.Test;
import org.lechuga.EntityManager;
import org.lechuga.EntityManagerFactory;
import org.lechuga.Order;
import org.lechuga.anno.Enumerated;
import org.lechuga.anno.Generated;
import org.lechuga.anno.Id;
import org.lechuga.autogen.impl.HsqldbSequence;
import org.lechuga.reflect.Embbedable;
import org.lenteja.jdbc.DataAccesFacade;
import org.lenteja.jdbc.JdbcDataAccesFacade;
import org.lenteja.jdbc.script.SqlScriptExecutor;

public class ExpEntityManagerTest {

	final DataAccesFacade facade;

	public ExpEntityManagerTest() {
		final JDBCDataSource ds = new JDBCDataSource();
		ds.setUrl("jdbc:hsqldb:mem:movielens");
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

	@Embbedable
	public static class ExpId {

		private Integer idEns;

		private Long numExp;

		public Integer getIdEns() {
			return idEns;
		}

		public void setIdEns(Integer idEns) {
			this.idEns = idEns;
		}

		@Generated(value = HsqldbSequence.class, args = { "seq_exp" })
		public Long getNumExp() {
			return numExp;
		}

		public void setNumExp(Long numExp) {
			this.numExp = numExp;
		}

		@Override
		public String toString() {
			return "ExpId [idEns=" + idEns + ", numExp=" + numExp + "]";
		}
	}

	public static enum EFase {
		OK, FAILED;
	}

	public static class Exp {

		@Id
		public ExpId id;

		@Enumerated
		public EFase fase;

		public ExpId getId() {
			return id;
		}

		public void setId(ExpId id) {
			this.id = id;
		}

		public EFase getFase() {
			return fase;
		}

		public void setFase(EFase fase) {
			this.fase = fase;
		}

		@Override
		public String toString() {
			return "Exp [id=" + id + ", fase=" + fase + "]";
		}
	}

	@Test
	public void testName() throws Exception {

		EntityManager em = new EntityManagerFactory().buildEntityManager(facade, Exp.class);

		facade.begin();
		try {
			{
				final Exp exp1;
				{
					exp1 = new Exp();
					exp1.setId(new ExpId());
					exp1.getId().setIdEns(8201);
					exp1.getId().setNumExp(null);
					exp1.setFase(EFase.OK);
				}
				final Exp exp2;
				{
					exp2 = new Exp();
					exp2.setId(new ExpId());
					exp2.getId().setIdEns(8208);
					exp2.getId().setNumExp(null);
					exp2.setFase(EFase.FAILED);
				}

				assertEquals("Exp [id=ExpId [idEns=8201, numExp=null], fase=OK]", exp1.toString());
				assertEquals("Exp [id=ExpId [idEns=8208, numExp=null], fase=FAILED]", exp2.toString());

				em.store(exp1);
				em.store(exp2);
				assertEquals("Exp [id=ExpId [idEns=8201, numExp=100], fase=OK]", exp1.toString());
				assertEquals("Exp [id=ExpId [idEns=8208, numExp=101], fase=FAILED]", exp2.toString());

				assertEquals(
						"[Exp [id=ExpId [idEns=8201, numExp=100], fase=OK], "
								+ "Exp [id=ExpId [idEns=8208, numExp=101], fase=FAILED]]",
						em.loadAll(Exp.class, Order.by(Order.asc("id.idEns"), Order.asc("id.numExp"))).toString());
			}
			{
				ExpId id = new ExpId();
				id.setIdEns(8201);
				id.setNumExp(100L);

				Exp exp1 = em.loadById(Exp.class, id);
				Exp exp12 = em.loadById(Exp.class, id);

				exp1.setFase(EFase.FAILED);
				em.store(exp1);
				em.refresh(exp12);

				assertEquals("Exp [id=ExpId [idEns=8201, numExp=100], fase=FAILED]", exp1.toString());
				assertEquals("Exp [id=ExpId [idEns=8201, numExp=100], fase=FAILED]", exp12.toString());
			}
			{
				ExpId idExp1 = new ExpId();
				idExp1.setIdEns(8201);
				idExp1.setNumExp(100L);
				ExpId idExp2 = new ExpId();
				idExp2.setIdEns(8208);
				idExp2.setNumExp(101L);

				Exp exp1 = em.loadById(Exp.class, idExp1);

				assertTrue(em.exists(exp1));
				assertTrue(em.existsById(Exp.class, idExp2));

				em.delete(exp1);
				em.deleteById(Exp.class, idExp2);

				assertEquals("[]", em.loadAll(Exp.class).toString());

				assertFalse(em.exists(exp1));
				assertFalse(em.existsById(Exp.class, idExp2));

			}

			facade.commit();
		} catch (Exception e) {
			facade.rollback();
			throw e;
		}
	}
}
