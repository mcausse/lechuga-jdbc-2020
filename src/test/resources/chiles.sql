
drop sequence seq_chiles if exists;
create sequence seq_chiles start with 100;

drop sequence seq_chile_events if exists;
create sequence seq_chile_events start with 1000;

create table chiles (
	id integer primary key,
	parent integer,
	name varchar(1) not null,
	description varchar(500)
);
alter table chiles add foreign key (parent) references chiles(id);

create table chile_events (
	id integer primary key,
	id_chile integer not null,
	data timestamp not null,
	description varchar(500),
	photo_filename varchar(256)
);
alter table chile_events add foreign key (id_chile) references chiles(id);







