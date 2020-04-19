package org.lechuga.examples;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.Before;
import org.junit.Test;
import org.lechuga.EntityManager;
import org.lechuga.EntityManagerFactory;
import org.lechuga.anno.Column;
import org.lechuga.anno.Handler;
import org.lechuga.anno.Id;
import org.lechuga.anno.Table;
import org.lechuga.handler.ColumnHandler;
import org.lechuga.mql.QueryBuilder;
import org.lechuga.reflect.Embbedable;
import org.lenteja.jdbc.DataAccesFacade;
import org.lenteja.jdbc.JdbcDataAccesFacade;
import org.lenteja.jdbc.extractor.PageResult;
import org.lenteja.jdbc.extractor.Pager;
import org.lenteja.jdbc.script.SqlScriptExecutor;
import org.lenteja.jdbc.txproxy.TransactionalMethod;
import org.lenteja.jdbc.txproxy.TransactionalServiceProxyfier;

public class TestMovies {

	final DataAccesFacade facade;

	public TestMovies() {
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
			sql.runFromClasspath("movielens.sql");
			facade.commit();
		} catch (Throwable e) {
			facade.rollback();
			throw e;
		}
	}

	@Test
	public void testName() throws Exception {

		EntityManager em = new EntityManagerFactory().buildEntityManager(facade, Genre.class, GenreMovie.class,
				Movie.class, GenresCountDto.class);

		facade.begin();
		try {
			// TODO
			List<Movie> r = em.buildQuery() //
					.addAlias("m", Movie.class) //
					.addAlias("gm", GenreMovie.class) //
					.addAlias("g", Genre.class) //
					.append("select {m.*} from {m.#} ") //
					.append("join {gm.#} on {m.id}={gm.movieId} ") //
					.append("join {g.#} on {gm.genreId}={g.id} ") //
					.append("where {g.name=?}", "Sci-Fi ") //
					.append("order by {m.year} asc ") //
					.getExecutor(Movie.class) //
					.load() //
			;
			for (Movie m : r) {
				System.out.println(m);
			}

			// List<Movie> r2 = em.loadBy(Movie.class, q -> q.append("{model.year between ?
			// and ?}", 1979, 1982),
			// Order.by(Order.asc("model.title")));
			// for (Movie m : r2) {
			// System.out.println(m);
			// }

			facade.commit();
		} catch (Exception e) {
			facade.rollback();
			throw e;
		}
	}

	@Test
	public void testTransactionalService() throws Exception {

		EntityManager em = new EntityManagerFactory().buildEntityManager(facade, Genre.class, GenreMovie.class,
				Movie.class, GenresCountDto.class);

		MovieService service = TransactionalServiceProxyfier.proxyfy(facade, MovieService.class,
				new MovieServiceImpl(em));

		for (GenresCountDto g : service.getGenres()) {
			System.out.println(g);
		}

		for (Movie m : service.findMovies("man", 1900, 2030, 15L)) {
			System.out.println(m);
		}

		PageResult<Movie> firstPage = service.findMovies(new Pager<>(10, 0), "a", 1900, 2030, 15L);
		PageResult<Movie> lastPage = service.findMovies(new Pager<>(10, firstPage.getTotalPages() - 1), "a", 1900, 2030,
				15L);

		System.out.println(firstPage);
		System.out.println(lastPage);

	}

	public static interface MovieService {

		@TransactionalMethod(readOnly = true)
		public List<GenresCountDto> getGenres();

		@TransactionalMethod(readOnly = true)
		public List<Movie> findMovies(String titlePartial, Integer fromYear, Integer toYear, Long genreId);

		@TransactionalMethod(readOnly = true)
		public PageResult<Movie> findMovies(Pager<Movie> pager, String titlePartial, Integer fromYear, Integer toYear,
				Long genreId);
	}

	public static class GenresCountDto {

		public Genre genre;

		@Column("c")
		public Long count;

		@Override
		public String toString() {
			return "GenresCountDto [genre=" + genre + ", count=" + count + "]";
		}
	}

	public static class MovieServiceImpl implements MovieService {

		final EntityManager em;

		public MovieServiceImpl(EntityManager em) {
			super();
			this.em = em;
		}

		@Override
		public List<GenresCountDto> getGenres() {
			return em.buildQuery() //
					.addAlias("gm", GenreMovie.class) //
					.addAlias("g", Genre.class) //
					.append("select {g.*}, count(*) as c ") //
					.append("from {g.#} ") //
					.append("join {gm.#} on {g.id}={gm.genreId} ") //
					.append("group by {g.*} ") //
					.append("order by c desc ") //
					.getExecutor(GenresCountDto.class) //
					.load() //
			;
		}

		@Override
		public List<Movie> findMovies(String titlePartial, Integer fromYear, Integer toYear, Long genreId) {

			QueryBuilder q = em.buildQuery() //
					.addAlias("m", Movie.class) //
					.addAlias("gm", GenreMovie.class) //
					.append("select {m.*} ") //
					.append("from {m.#} join {gm.#} on {m.id}={gm.movieId} ") //
					.append("where 1=1 ");

			if (titlePartial != null && !titlePartial.isEmpty()) {
				// XXX tela
				q.append(" and upper({m.title) like upper(?})", "%" + titlePartial + "%");
			}
			if (fromYear != null) {
				q.append(" and {m.year >= ?}", fromYear);
			}
			if (toYear != null) {
				q.append(" and {m.year <= ?}", toYear + 1);
			}
			if (genreId != null) {
				q.append(" and {gm.genreId=?}", genreId);
			}

			q.append(" order by {m.year} asc");

			return q.getExecutor(Movie.class).load();
		}

		@Override
		public PageResult<Movie> findMovies(Pager<Movie> pager, String titlePartial, Integer fromYear, Integer toYear,
				Long genreId) {

			QueryBuilder q = em.buildQuery() //
					.addAlias("m", Movie.class) //
					.addAlias("gm", GenreMovie.class) //
					.append("select {m.*} ") //
					.append("from {m.#} join {gm.#} on {m.id}={gm.movieId} ") //
					.append("where 1=1 ");

			if (titlePartial != null && !titlePartial.isEmpty()) {
				// XXX tela
				q.append(" and upper({m.title) like upper(?})", "%" + titlePartial + "%");
			}
			if (fromYear != null) {
				q.append(" and {m.year >= ?}", fromYear);
			}
			if (toYear != null) {
				q.append(" and {m.year <= ?}", toYear + 1);
			}
			if (genreId != null) {
				q.append(" and {gm.genreId=?}", genreId);
			}

			q.append(" order by {m.year} asc");

			return q.getExecutor(Movie.class).loadPage(pager);
		}

	}

	@Table("genres")
	@Embbedable
	public static class Genre {

		@Id
		public Long id;

		public String name;

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (id == null ? 0 : id.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			Genre other = (Genre) obj;
			if (id == null) {
				if (other.id != null) {
					return false;
				}
			} else if (!id.equals(other.id)) {
				return false;
			}
			return true;
		}

		@Override
		public String toString() {
			return "Genre [id=" + id + ", name=" + name + "]";
		}
	}

	@Table("genres_movies")
	public static class GenreMovie {

		@Id
		public Long id;

		public Long movieId;
		public Long genreId;

		@Override
		public String toString() {
			return "GenreMovie [id=" + id + ", movieId=" + movieId + ", genreId=" + genreId + "]";
		}
	}

	@Table("movies")
	public static class Movie {

		@Id
		public Long id;

		public String title;

		@Column("release_date")
		@Handler(YearDateHandler.class)
		public Integer year;

		@Override
		public String toString() {
			return "Movie [id=" + id + ", title=" + title + ", year=" + year + "]";
		}
	}

	public static class YearDateHandler implements ColumnHandler {

		protected final SimpleDateFormat formatter = new SimpleDateFormat("yyyy");

		@Override
		public Object readValue(ResultSet rs, String columnName) throws SQLException {

			Date value = rs.getTimestamp(columnName);
			if (value == null) {
				return null;
			}
			return Integer.valueOf(formatter.format(value));
		}

		@Override
		public Object getJdbcValue(Object value) {
			if (value == null) {
				return null;
			}
			try {
				return formatter.parse(String.valueOf(value));
			} catch (final ParseException e) {
				throw new RuntimeException(e);
			}
		}

	}

}
