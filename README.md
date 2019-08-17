# PlantUML2DDL
Generate DDL(**Now only support Mysql**) from PlantUML [Entity Relationship Diagram](http://plantuml.com/zh/ie-diagram)

## Usage
1. Install the plugin in Intellij Idea or install [plugin](https://github.com/xwsg/plantuml2ddl/releases) from disk
2. Open a PlantUML file
3. Right-click on the PlantUML file or Alt-Insert -> Generate -> Generate DDL from PlantUML.

The plugin will generate a {PlantUML file}.sql file in the same directory.

## Defining Symbol
Symbol| Explain
------|-----
\# | PRIMARY KEY
\* | NOT NULL
<\<generated>> | AUTO_INCREMENT
<\<notnull>> | NOT NULL
<\<default:{DEFAULT_VALUE}>> | DEFAULT {DEFAULT_VALUE}
--{COLUMN_COMMENT} | column COMMENT '{COLUMN_COMMENT}'
{TABLE_COMMENT} <br> --/../==/__ | table COMMENT '{TABLE_COMMENT}'

## Example
For example: `mall.puml`

```
@startuml

' hide the spot
hide circle

' avoid problems with angled crows feet
skinparam linetype ortho

entity "tbl_user" as user {
  用户表
  --
  #id : bigint(20) <<generated>>
  --
  *type : tinyint(4) <<default:0>> --用户类型：0-PC用户,1-移动端用户
  *name : varchar(50) <<default:'anonymous'>> --用户名
  description : varchar(200) <<default:'some string'>> --用户描述
}

entity "tbl_order" as order {
  订单表
  ==
  #id : bigint(20) <<generated>>
  --
  * **order_number** : varchar(20)  <<default:'0'>> -- 订单号
  *user_id : bigint(20) <<FK>> <<default:0>> -- 用户id
  *item_id: bigint(20) <<FK>> <<default:0>> -- 商品id
}

entity "tbl_item" as item {
  商品表
  ..
  #id : bigint(20)  <<generated>>
  --
  title : varchar(50)  <<default: 'wahaha'>> <<notnull>> -- 商品标题
  *price : int(11) <<default: 0>> -- 商品价格
}

user }|..|{ order
item }|..|{ order

@enduml
```

![plantuml2ddl](plantuml2ddl.gif)

Will generate a file `mall-{yyyyMMddHHmmss}.sql`, content:
```
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
```
