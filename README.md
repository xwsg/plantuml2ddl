# PlantUML2DDL
Generate DDL from PlantUML

## Usage
1. Install the plugin in Intellij Idea
2. Open a PlantUML file
3. Right-click on the PlantUML file or Alt-Insert -> Generate -> Generate DDL from PlantUML.

The plugin will generate a {PlantUML file}.sql file in the same directory.

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
  *order_number : varchar(20)
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

Will generate a file `mall.sql`, content:
```
CREATE TABLE IF NOT EXISTS `tbl_user` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(50) NOT NULL,
  `description` VARCHAR(200),
  PRIMARY KEY (`id`)
);
CREATE TABLE IF NOT EXISTS `tbl_order` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `order_number` VARCHAR(20) NOT NULL,
  `user_id` BIGINT(20) NOT NULL,
  `item_id:` <<FK>> NOT NULL,
  PRIMARY KEY (`id`)
);
CREATE TABLE IF NOT EXISTS `tbl_item` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `title` VARCHAR(50) NOT NULL,
  `price` INT(11) NOT NULL,
  PRIMARY KEY (`id`)
);
```