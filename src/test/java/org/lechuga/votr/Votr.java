package org.lechuga.votr;

import java.util.Date;

import org.lechuga.anno.Column;
import org.lechuga.anno.Generated;
import org.lechuga.anno.Id;
import org.lechuga.anno.Table;
import org.lechuga.autogen.impl.HsqldbSequence;

// create table votrs (
// votr_id smallint primary key,
// votr_hash varchar(15) not null,
// title varchar(100) not null,
// descr varchar(500) not null,
// creat_date timestamp not null
// );
@Table("votrs")
public class Votr {

	@Id
	@Generated(value = HsqldbSequence.class, args = { "seq_votrs" })
	public Integer votrId;

	public String votrHash;
	public String title;
	@Column("descr")
	public String description;
	public Date creatDate;
}