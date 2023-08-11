create table "tbl_user" (
    "id" bigint(20) not null primary key,
    "type" tinyint(4) default 0 not null,
    "name" varchar(50) default 'anonymous' not null,
    "description" varchar(200) default 'some string'
);
comment on table "tbl_user" is 'table for user';
comment on column "tbl_user"."type" is 'user type: 0-pc,1-mobile';
comment on column "tbl_user"."name" is 'user name';
comment on column "tbl_user"."description" is 'user description';

create table "tbl_order" (
    "id" bigint(20) not null primary key,
    "order_number" varchar(20) default '0' not null,
    "user_id" bigint(20) default 0 not null,
    "item_id" bigint(20) default 0 not null
);
comment on table "tbl_order" is 'table for order';
comment on column "tbl_order"."order_number" is 'order number';
comment on column "tbl_order"."user_id" is 'user id';
comment on column "tbl_order"."item_id" is 'item id';

create table "tbl_item" (
    "id" bigint(20) not null primary key,
    "title" varchar(50) default 'wahaha' not null,
    "price" int(11) default 0 not null
);
comment on table "tbl_item" is 'table for item';
comment on column "tbl_item"."title" is 'item title';
comment on column "tbl_item"."price" is 'item price';

