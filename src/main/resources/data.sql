drop table if exists hello;

CREATE TABLE hello (
  id serial PRIMARY KEY,
  message varchar(255)
);

insert into hello (message) values ('Hello World!');
insert into hello (message) values ('Hello World Again!');