
drop table dogs if exists;
drop table masters if exists;

create table dogs (
	id_dog integer primary key,
	name varchar(15) not null,
	age smallint not null,
	id_master smallint not null,
);
create table masters (
	id_master smallint primary key,
	name varchar(15) not null
);

insert into masters (id_master,name) values (100,'M');
insert into dogs (id_dog,name,age,id_master) values (1,'chucho',12,100);


