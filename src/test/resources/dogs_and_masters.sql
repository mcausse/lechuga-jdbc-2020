
drop sequence seq_dogs if exists;
create sequence seq_dogs start with 100;

drop sequence seq_masters if exists;
create sequence seq_masters start with 10;



drop table dogs if exists;
drop table masters if exists;

create table dogs (
	id_dog integer primary key,
	name varchar(15) not null,
	id_master smallint not null,
);
create table masters (
	id_master smallint primary key,
	name varchar(15) not null
);

