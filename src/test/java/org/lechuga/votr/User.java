package org.lechuga.votr;

import java.util.Date;

import org.lechuga.anno.Column;
import org.lechuga.anno.Generated;
import org.lechuga.anno.Id;
import org.lechuga.anno.Table;
import org.lechuga.autogen.impl.HsqldbSequence;

// create table users (
// user_id integer primary key,
// user_hash varchar(15) not null,
// email varchar(100) not null,
// alias varchar(100),
//
// votr_id smallint not null,
//
// option_norder integer,
// option_date timestamp
// );

@Table("users")
public class User {

	@Id
	@Generated(value = HsqldbSequence.class, args = { "seq_users" })
	@Column("user_id")
	public Long idUser;

	public String userHash;
	public String email;
	public String alias;

	public Integer votrId;

	public Long optionNorder;
	public Date optionDate;
}