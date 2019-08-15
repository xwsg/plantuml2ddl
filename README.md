# PlantUML2DDL
Generate DDL(**Now only support Mysql**) from PlantUML [Entity Relationship Diagram](http://plantuml.com/zh/ie-diagram)

## Usage
1. Install the plugin in Intellij Idea or install [plugin](plantuml2ddl.jar) from disk
2. Open a PlantUML file
3. Right-click on the PlantUML file or Alt-Insert -> Generate -> Generate DDL from PlantUML.

The plugin will generate a {PlantUML file}.sql file in the same directory.

## Defining Symbol
Symbol| Explain
------|-----
\# | PRIMARY KEY
\* | NOT NULL
<\<generated>> | AUTO_INCREMENT

## Example
For example: `mall.puml`

```
@startuml

' hide the spot
hide circle

' avoid problems with angled crows feet
skinparam linetype ortho

entity "tbl_user" as user {
  #id : bigint(20) <<generated>>
  --
  *name : varchar(50)
  description : varchar(200)
}

entity "tbl_order" as order {
  #id : bigint(20) <<generated>>
  --
  * **order_number** : varchar(20)
  *user_id : bigint(20) <<FK>>
  *item_id: bigint(20) <<FK>>
}

entity "tbl_item" as item {
  #id : bigint(20)  <<generated>>
  --
  *title : varchar(50)
  *price : int(11)
}

user }|..|{ order
item }|..|{ order

@enduml
```

![plantuml2ddl](plantuml2ddl.gif)

Will generate a file `mall.sql`, content:
```
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
```
