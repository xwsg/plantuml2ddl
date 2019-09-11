CREATE TABLE IF NOT EXISTS `tbl_user` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
    `type` TINYINT(4) NOT NULL DEFAULT 0 COMMENT '用户类型：0-PC用户,1-移动端用户',
    `name` VARCHAR(50) NOT NULL DEFAULT 'anonymous' COMMENT '用户名',
    `description` VARCHAR(200) DEFAULT 'some string' COMMENT '用户描述',
    PRIMARY KEY (`id`)
) COMMENT '用户表';

CREATE TABLE IF NOT EXISTS `tbl_order` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
    `order_number` VARCHAR(20) NOT NULL DEFAULT '0' COMMENT '订单号',
    `user_id` BIGINT(20) NOT NULL DEFAULT 0 COMMENT '用户id',
    `item_id` BIGINT(20) NOT NULL DEFAULT 0 COMMENT '商品id',
    PRIMARY KEY (`id`)
) COMMENT '订单表';

CREATE TABLE IF NOT EXISTS `tbl_item` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
    `title` VARCHAR(50) NOT NULL DEFAULT 'wahaha' COMMENT '商品标题',
    `price` INT(11) NOT NULL DEFAULT 0 COMMENT '商品价格',
    PRIMARY KEY (`id`)
) COMMENT '商品表';

