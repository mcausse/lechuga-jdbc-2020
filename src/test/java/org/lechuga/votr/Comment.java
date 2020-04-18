package org.lechuga.votr;

import java.util.Date;

import org.lechuga.anno.Column;
import org.lechuga.anno.Generated;
import org.lechuga.anno.Id;
import org.lechuga.anno.Table;
import org.lechuga.autogen.impl.HsqldbSequence;

// create table comments (
// comment_id integer primary key,
//
// comment_date timestamp not null,
// comment varchar(1024) not null,
//
// votr_id smallint not null,
// user_id integer not null
// );
@Table("comments")
public class Comment {

	@Id
	@Generated(value = HsqldbSequence.class, args = { "seq_comments" })
	public Long commentId;

	public Date commentDate;
	@Column("comment")
	public String text;

	public Integer votrId;
	public Long userId;
}