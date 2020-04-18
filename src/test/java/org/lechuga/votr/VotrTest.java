package org.lechuga.votr;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.Before;
import org.junit.Test;
import org.lechuga.EntityManager;
import org.lechuga.EntityManagerFactory;
import org.lechuga.GenericDao;
import org.lenteja.jdbc.DataAccesFacade;
import org.lenteja.jdbc.JdbcDataAccesFacade;
import org.lenteja.jdbc.script.SqlScriptExecutor;
import org.lenteja.jdbc.txproxy.TransactionalMethod;
import org.lenteja.jdbc.txproxy.TransactionalServiceProxyfier;

public class VotrTest {

	final DataAccesFacade facade;

	public VotrTest() {
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
			sql.runFromClasspath("votr.sql");
			facade.commit();
		} catch (Throwable e) {
			facade.rollback();
			throw e;
		}
	}

	@Test
	public void testName() throws Exception {

		EntityManager em = new EntityManagerFactory().buildEntityManager(facade, Votr.class, User.class, Option.class,
				Comment.class);

		MovieService service = TransactionalServiceProxyfier.proxyfy(facade, new MovieServiceImpl(em),
				MovieService.class);

		String hashVotr;
		{
			Votr v = new Votr();
			v.title = "best poem";
			v.description = "best poem description";
			User creator = new User();
			creator.email = "mhc@votr.org";
			service.createVotr(v, creator);
			hashVotr = v.votrHash;
		}
		User mem = new User();
		mem.email = "mem@votr.com";
		service.convidaUser(hashVotr, mem);
	}

	public static interface MovieService {

		/**
		 * Crea votació base amb usuari creador. Inserta missatge de creació,
		 */
		@TransactionalMethod
		void createVotr(Votr v, User creator);

		@TransactionalMethod
		void convidaUser(String hashVotr, User user);

		@TransactionalMethod
		void expulsaUser(String hashVotr, String hashUser);

		/**
		 * desvota a tots els usuaris. borra les opcions que ja existeixin. Inserta les
		 * opcions, enumerant-les. comenta.
		 */
		@TransactionalMethod
		void creaOptions(String hashVotr, List<Option> option, String hashUserModifier);

		/**
		 * @param alias si null, esborra alias
		 */
		@TransactionalMethod
		void userUpdateAlias(String hashVotr, String hashUser, String alias);

		/**
		 * @param optionOrderVotat si null, desvota.
		 */
		@TransactionalMethod
		void userVota(String hashVotr, String hashUser, Long optionOrderVotat);

		@TransactionalMethod
		void userComenta(String hashVotr, String hashUser, String comment);

		@TransactionalMethod(readOnly = true)
		VotrInfo getVotrInfo(String hashVotr, String hashUser);
	}

	public static class VotrInfo {

		public final Votr votr;
		public final User you;
		public final List<User> allUsers;
		public final Map<Option, List<User>> optionsVots;
		public final List<Comment> comments;

		public VotrInfo(Votr votr, User you, List<User> allUsers, Map<Option, List<User>> optionsVots,
				List<Comment> comments) {
			super();
			this.votr = votr;
			this.you = you;
			this.allUsers = allUsers;
			this.optionsVots = optionsVots;
			this.comments = comments;
		}

		@Override
		public String toString() {
			return "VotrInfo [votr=" + votr + ", you=" + you + ", allUsers=" + allUsers + ", optionsVots=" + optionsVots
					+ ", comments=" + comments + "]";
		}
	}

	public static class A {
		public static void assertNull(Object value, String valueDescription) {
			if (value != null) {
				throw new IllegalArgumentException("'" + valueDescription + "' must be null");
			}
		}

		public static void assertNotNull(Object value, String valueDescription) {
			if (value == null) {
				throw new IllegalArgumentException("'" + valueDescription + "' must be not null");
			}
		}

		public static void assertNotEmpty(String value, String valueDescription) {
			if (value == null || value.isEmpty()) {
				throw new IllegalArgumentException("'" + valueDescription + "' must be not null");
			}
		}
	}

	public static class MovieServiceImpl implements MovieService {

		final VotrDao votrDao;
		final UserDao userDao;
		final GenericDao<Option, OptionId> optionDao;
		final GenericDao<Comment, Long> commentDao;

		public MovieServiceImpl(EntityManager em) {
			super();
			this.votrDao = new VotrDao(em);
			this.userDao = new UserDao(em);
			this.optionDao = new GenericDao<>(em, Option.class);
			this.commentDao = new GenericDao<>(em, Comment.class);
		}

		protected String generaHash(String input) {
			return Integer.toHexString(input.hashCode());
		}

		@Override
		public void createVotr(Votr v, User creator) {
			A.assertNull(v.votrId, Votr.class.getName() + ".votrId");
			A.assertNull(v.votrHash, Votr.class.getName() + ".votrHash");
			A.assertNotEmpty(v.title, Votr.class.getName() + ".title");
			A.assertNotEmpty(v.description, Votr.class.getName() + ".description");
			A.assertNull(v.creatDate, Votr.class.getName() + ".creatDate");

			v.creatDate = new Date();
			v.votrHash = generaHash(v.title + v.description + v.creatDate);
			votrDao.insert(v);

			addUser(v, creator);

			addComment(v.votrId, creator.idUser, "He creat la votació '" + v.title + "'.");
		}

		private void addComment(int votrId, long idUser, String text) {
			Comment c = new Comment();
			c.votrId = votrId;
			c.userId = idUser;
			c.commentDate = new Date();
			c.text = text;
			commentDao.insert(c);
		}

		private void addUser(Votr v, User user) {
			A.assertNull(user.idUser, User.class.getName() + ".creator.");
			A.assertNull(user.userHash, User.class.getName() + ".userHash");
			A.assertNull(user.optionNorder, User.class.getName() + ".optionNorder");
			A.assertNotEmpty(user.email, User.class.getName() + ".email");

			user.userHash = generaHash(v.votrHash + user.email);
			user.votrId = v.votrId;
			userDao.insert(user);

			addComment(v.votrId, user.idUser, "M'afegeixo.");
		}

		@Override
		public void convidaUser(String hashVotr, User user) {
			Votr v = votrDao.loadUniqueByHash(hashVotr);
			addUser(v, user);
		}

		@Override
		public void expulsaUser(String hashVotr, String hashUser) {
			User u = userDao.loadUniqueByHash(hashVotr, hashUser);
			addComment(u.votrId, u.idUser, "M'expulsen.");
			userDao.delete(u);
		}

		@Override
		public void creaOptions(String hashVotr, List<Option> option, String hashUserModifier) {
			// TODO Auto-generated method stub

		}

		@Override
		public void userUpdateAlias(String hashVotr, String hashUser, String alias) {
			// TODO Auto-generated method stub

		}

		@Override
		public void userVota(String hashVotr, String hashUser, Long optionOrderVotat) {
			// TODO Auto-generated method stub

		}

		@Override
		public void userComenta(String hashVotr, String hashUser, String comment) {
			// TODO Auto-generated method stub

		}

		@Override
		public VotrInfo getVotrInfo(String hashVotr, String hashUser) {
			// TODO Auto-generated method stub
			return null;
		}

	}

}