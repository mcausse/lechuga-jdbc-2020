package org.lenteja.jdbc.extractor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

import org.lenteja.jdbc.Mapable;

public class EntityMapResultSetExtractor<K, E> implements Mapable<Map<K, E>> {

	final Mapable<E> rowMapper;
	final Function<E, K> byColumn;

	public EntityMapResultSetExtractor(Mapable<E> rowMapper, Function<E, K> byColumn) {
		super();
		this.rowMapper = rowMapper;
		this.byColumn = byColumn;
	}

	@Override
	public Map<K, E> map(ResultSet rs) throws SQLException {
		Map<K, E> r = new LinkedHashMap<>();

		while (rs.next()) {
			E row = rowMapper.map(rs);
			K key = byColumn.apply(row);
			r.put(key, row);
		}

		return r;
	}

}