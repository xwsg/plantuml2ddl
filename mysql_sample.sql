create table if not exists `tbl_user` (
    `id` bigint(20) not null auto_increment,
    `type` tinyint(4) not null default 0 comment 'user type: 0-pc,1-mobile',
    `name` varchar(50) not null default 'anonymous' comment 'user name',
    `description` varchar(200) default 'some string' comment 'user description',
    primary key (`id`)
) comment 'table for user';

create table if not exists `tbl_order` (
    `id` bigint(20) not null auto_increment,
    `order_number` varchar(20) not null default '0' comment 'order number',
    `user_id` bigint(20) not null default 0 comment 'user id',
    `item_id` bigint(20) not null default 0 comment 'item id',
    primary key (`id`)
) comment 'table for order';

create table if not exists `tbl_item` (
    `id` bigint(20) not null auto_increment,
    `title` varchar(50) not null default 'wahaha' comment 'item title',
    `price` int(11) not null default 0 comment 'item price',
    primary key (`id`)
) comment 'table for item';

