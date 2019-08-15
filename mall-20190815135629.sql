CREATE TABLE IF NOT EXISTS `tbl_user` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `name` varchar(50) NOT NULL,
    `description` varchar(200),
    PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `tbl_order` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `order_number` varchar(20) NOT NULL,
    `user_id` bigint(20) NOT NULL,
    `item_id` bigint(20) NOT NULL,
    PRIMARY KEY (`id`)
);

CREATE TABLE IF NOT EXISTS `tbl_item` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `title` varchar(50) NOT NULL,
    `price` int(11) NOT NULL,
    PRIMARY KEY (`id`)
);

