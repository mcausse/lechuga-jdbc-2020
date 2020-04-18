package org.lenteja.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Aquesta interfície tributa la capacitat d'extreure un valor tipat d'un
 * {@link ResultSet}. Aquest valor pot ser una entitat, o un valor escalar.
 *
 * @author mhoms
 *
 * @param <T> resultat del mapping
 */
public interface Mapable<T> {

	T map(ResultSet rs) throws SQLException;

}