CREATE ROLE mplayer LOGIN
  PASSWORD 'mplayeradmin'
  SUPERUSER INHERIT NOCREATEDB NOCREATEROLE NOREPLICATION;

CREATE DATABASE mplayer
  WITH OWNER = mplayer
       ENCODING = 'UTF8'
       TABLESPACE = pg_default
       LC_COLLATE = 'Ukrainian_Ukraine.1251'
       LC_CTYPE = 'Ukrainian_Ukraine.1251'
       CONNECTION LIMIT = -1;

create table users (
	id bigserial not null,
	name varchar(128) not null,
	password varchar(128) not null,
	regdate timestamp without time zone not null default now(),
	constraint pk_users primary key (id)
);

insert into users 
(name, password)
values
('serejja', md5('furnok'));