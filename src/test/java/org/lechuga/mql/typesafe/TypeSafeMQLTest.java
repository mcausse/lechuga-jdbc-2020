package org.lechuga.mql.typesafe;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.lechuga.EntityManager;
import org.lechuga.EntityManagerFactory;
import org.lechuga.mql.QueryBuilder;
import org.lechuga.pizza.ExpEntityManagerTest.EFase;
import org.lechuga.pizza.ExpEntityManagerTest.Exp;
import org.lechuga.pizza.PizzaEntityManagerTest.Pizza;

public class TypeSafeMQLTest {

	@Test
	public void testName() throws Exception {

		EntityManager em = new EntityManagerFactory().buildEntityManager(null, Exp.class, Pizza.class);

		{
			QueryBuilder qb = em.buildQuery();
			Predicates<Exp> pexp = qb.addAliasAndBuildPredicates("e", Exp.class);
			qb.append("select * from {} where {}", pexp, pexp.eq(Exp_.fase, EFase.OK));
			assertEquals("select * from exp e where e.fase=? -- [OK(String)]", qb.getQueryObject().toString());
		}
		{
			QueryBuilder qb = em.buildQuery();
			Predicates<Exp> pexp = qb.addAliasAndBuildPredicates("e", Exp.class);

			qb.append("select * from {} where {}", pexp, pexp.eq(Exp_.fase, EFase.OK));
			assertEquals("select * from exp e where e.fase=? -- [OK(String)]", qb.getQueryObject().toString());
		}
		{
			QueryBuilder qb = em.buildQuery();
			Predicates<Exp> pexp1 = qb.addAliasAndBuildPredicates("e1", Exp.class);
			Predicates<Exp> pexp2 = qb.addAliasAndBuildPredicates("e2", Exp.class);

			qb.append("select * from {} join {} on {}", pexp1, pexp2, pexp1.eq(Exp_.fase, pexp2, Exp_.fase));
			assertEquals("select * from exp e1 join exp e2 on e1.fase=e2.fase -- []", qb.getQueryObject().toString());
		}

		{
			QueryBuilder qb = em.buildQuery();
			Predicates<Exp> pexp = qb.addAliasAndBuildPredicates("e", Exp.class);

			qb.append("{}", Restrictions.and(pexp.between(Exp_.numExp, 100L, 200L),
					pexp.in(Exp_.fase, EFase.OK, EFase.FAILED)));
			assertEquals(
					"e.num_exp between ? and ? and e.fase in (?,?) -- [100(Long), 200(Long), OK(String), FAILED(String)]",
					qb.getQueryObject().toString());
		}

		{
			QueryBuilder qb = em.buildQuery();
			Predicates<Exp> pexp = qb.addAliasAndBuildPredicates("e", Exp.class);

			qb.append("{}", Restrictions.not(Restrictions.or(pexp.between(Exp_.numExp, 100L, 200L),
					pexp.notIn(Exp_.fase, EFase.OK, EFase.FAILED))));
			assertEquals(
					"not(e.num_exp between ? and ? or e.fase not in (?,?)) -- [100(Long), 200(Long), OK(String), FAILED(String)]",
					qb.getQueryObject().toString());
		}

		{
			QueryBuilder qb = em.buildQuery();
			Predicates<Pizza> ppizza = qb.addAliasAndBuildPredicates("p", Pizza.class);

			qb.append("{}", ppizza.ilike(Pizza_.name, ELike.CONTAINS, "man"));
			assertEquals("upper(p.name) like upper(?) -- [%man%(String)]", qb.getQueryObject().toString());
		}
		{
			QueryBuilder qb = em.buildQuery();
			Predicates<Pizza> ppizza = qb.addAliasAndBuildPredicates("p", Pizza.class);

			qb.append("{}", ppizza.like(Pizza_.name, ELike.CONTAINS, "man"));
			assertEquals("p.name like ? -- [%man%(String)]", qb.getQueryObject().toString());
		}
		{
			QueryBuilder qb = em.buildQuery();
			Predicates<Pizza> ppizza = qb.addAliasAndBuildPredicates("p", Pizza.class);

			qb.append("{}",
					Restrictions.or(Restrictions.not(ppizza.isNotNull(Pizza_.name)), ppizza.isNull(Pizza_.name)));
			assertEquals("not(p.name IS NOT NULL) or p.name IS NULL -- []", qb.getQueryObject().toString());
		}
	}

	public interface Exp_ {
		public static final FieldDef<Integer, Exp> idEns = new FieldDef<>(Exp.class, "id.idEns");
		public static final FieldDef<Long, Exp> numExp = new FieldDef<>(Exp.class, "id.numExp");
		public static final FieldDef<EFase, Exp> fase = new FieldDef<>(Exp.class, "fase");
	}

	public interface Pizza_ {
		public static final FieldDef<Long, Pizza> idPizza = new FieldDef<>(Pizza.class, "idPizza");
		public static final FieldDef<String, Pizza> name = new FieldDef<>(Pizza.class, "name");
		public static final FieldDef<Double, Pizza> price = new FieldDef<>(Pizza.class, "price");
	}

}
