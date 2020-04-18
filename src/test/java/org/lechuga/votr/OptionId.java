package org.lechuga.votr;

import org.lechuga.reflect.Embbedable;

// create table options (
// votr_id smallint,
// norder integer,
//
// title varchar(100) not null,
// descr varchar(500) not null,
// );
@Embbedable
public class OptionId {
	public Integer votrId;
	public Long norder;
}