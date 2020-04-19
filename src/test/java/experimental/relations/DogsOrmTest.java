package org;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
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
import org.lechuga.anno.Transient;
import org.lechuga.autogen.impl.HsqldbSequence;
import org.lenteja.jdbc.DataAccesFacade;
import org.lenteja.jdbc.JdbcDataAccesFacade;
import org.lenteja.jdbc.script.SqlScriptExecutor;

public class DogsOrmTest {

	final DataAccesFacade facade;

	public DogsOrmTest() {
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
			SqlScriptExecutor sql = new SqlScriptExecutor(facade);
			sql.runFromClasspath("dogs_and_masters.sql");
			facade.commit();
		} catch (Throwable e) {
			facade.rollback();
			throw e;
		}
	}

	public static class MasterListener extends EntityListener<Master> {

		protected List<Dog> getDogsOf(EntityManager em, Master entity) {
			LazyPersistentList<Dog> l = new LazyPersistentList<Dog>() {
				@Override
				public List<Dog> load() {
					return em.buildQuery() //
							.addAlias("d", Dog.class) //
							.append("select {d.*} from {d.#} ") //
							.append("where {d.idMaster=?} ", entity.getIdMaster()) //
							.append("order by {d.idDog} asc") //
							.getExecutor(Dog.class) //
							.load();
				}
			};

			return l;
		}

		@Override
		public void afterLoad(EntityManager em, Master entity) {
			List<Dog> dogs = getDogsOf(em, entity);
			entity.setDogs(dogs);
		}

		@Override
		public void afterStore(EntityManager em, Master entity) {
			for (Dog dog : entity.getDogs()) {
				dog.setIdMaster(entity.getIdMaster());
				em.store(dog);
			}
		}

		@Override
		public void beforeDelete(EntityManager em, Master entity) {
			for (Dog dog : getDogsOf(em, entity)) {
				em.delete(dog);
			}
		}
	}

	@EntityListeners({ MasterListener.class })
	@Table("masters")
	public static class Master {

		@Id
		@Generated(value = HsqldbSequence.class, args = { "seq_masters" })
		Integer idMaster;
		String name;

		@Transient
		List<Dog> dogs;

		public Master() {
			super();
		}

		public Master(String name) {
			super();
			this.name = name;
		}

		public Integer getIdMaster() {
			return idMaster;
		}

		public void setIdMaster(Integer idMaster) {
			this.idMaster = idMaster;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public List<Dog> getDogs() {
			return dogs;
		}

		public void setDogs(List<Dog> dogs) {
			this.dogs = dogs;
		}

		@Override
		public String toString() {
			return String.format("Master [%s:%s:%s]", idMaster, name, dogs);
		}
	}

	@Table("dogs")
	public static class Dog {

		@Id
		@Generated(value = HsqldbSequence.class, args = { "seq_dogs" })
		Long idDog;
		String name;
		Integer idMaster;

		public Dog() {
			super();
		}

		public Dog(String name) {
			super();
			this.name = name;
		}

		public Long getIdDog() {
			return idDog;
		}

		public void setIdDog(Long idDog) {
			this.idDog = idDog;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public Integer getIdMaster() {
			return idMaster;
		}

		public void setIdMaster(Integer idMaster) {
			this.idMaster = idMaster;
		}

		@Override
		public String toString() {
			return String.format("Dog [%s:%s:%s]", idDog, name, idMaster);
		}
	}

	@Test
	public void testName() throws Exception {

		EntityManager em = new EntityManagerFactory().buildEntityManager(facade, Master.class, Dog.class);
		facade.begin();
		try {

			Master m;
			{
				Dog d1 = new Dog("faria");
				Dog d2 = new Dog("chucho");

				m = new Master("mhc");
				m.setDogs(Arrays.asList(d1, d2));

				em.store(m);
				assertEquals("Master [10:mhc:[Dog [100:faria:10], Dog [101:chucho:10]]]", m.toString());
			}
			{
				m = em.loadById(Master.class, m.getIdMaster());
				assertEquals("Master [10:mhc:[Dog [100:faria:10], Dog [101:chucho:10]]]", m.toString());

				m.setName("mhoms");
				m.getDogs().get(0).setName("din");
				em.store(m);
				m = em.loadById(Master.class, m.getIdMaster());
				assertEquals("Master [10:mhoms:[Dog [100:din:10], Dog [101:chucho:10]]]", m.toString());
			}
			{
				em.delete(m);

				assertEquals("[]", em.loadAll(Master.class).toString());
				assertEquals("[]", em.loadAll(Dog.class).toString());
			}

			facade.commit();
		} catch (Throwable e) {
			facade.rollback();
			throw e;
		}
	}
}
