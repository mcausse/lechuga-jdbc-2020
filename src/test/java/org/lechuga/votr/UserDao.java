package org.lechuga.votr;

import org.lechuga.EntityManager;
import org.lechuga.GenericDao;

public class UserDao extends GenericDao<User, Long> {

	public UserDao(EntityManager em) {
		super(em);
	}

	public User loadUniqueByHash(String votrHash, String userHash) {
		return getEntityManager().buildQuery() //
				.addAlias("v", Votr.class) //
				.addAlias("u", User.class) //
				.append("select {u.*} from {u.#} join {v.#} on {u.votrId}={v.votrId} ") //
				.append("where {v.votrHash=?} and {u.userHash=?}", votrHash, userHash) //
				.getExecutor(User.class) //
				.loadUnique();
	}

}
