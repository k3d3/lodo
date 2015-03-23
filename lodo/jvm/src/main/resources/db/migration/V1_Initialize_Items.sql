create table Item (
  parent uuid,
  timestamp bigint not null,
  contents varchar(128) not null,
  id uuid not null,
  complete boolean not null
);