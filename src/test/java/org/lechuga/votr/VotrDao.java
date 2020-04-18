package org.lechuga.votr;

import org.lechuga.EntityManager;
import org.lechuga.GenericDao;

public class VotrDao extends GenericDao<Votr, Integer> {

	public VotrDao(EntityManager em) {
		super(em);
	}

	public Votr loadUniqueByHash(String votrHash) {
		return getEntityManager().buildQuery() //
				.addAlias("v", Votr.class) //
				.append("select {v.*} from {v.#} where {v.votrHash=?}", votrHash) //
				.getExecutor(Votr.class) //
				.loadUnique();
	}

}
