package com.github.xwsg.plantuml.generator.mysql;

import com.alibaba.druid.DbType;
import com.github.xwsg.plantuml.generator.AbstractUmlGenerator;

public class MySQL2PlantUmlGenerator extends AbstractUmlGenerator {

    @Override
    protected DbType dbType() {
        return DbType.mysql;
    }

    @Override
    protected String trimQuote(String str) {
        return str.replaceAll("`", "");
    }
}
