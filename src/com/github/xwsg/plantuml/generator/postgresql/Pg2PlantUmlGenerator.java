package com.github.xwsg.plantuml.generator.postgresql;

import com.alibaba.druid.DbType;
import com.github.xwsg.plantuml.generator.AbstractUmlGenerator;

public class Pg2PlantUmlGenerator extends AbstractUmlGenerator {

    @Override
    protected DbType dbType() {
        return DbType.postgresql;
    }

    @Override
    protected String trimQuote(String str) {
        return str.replaceAll("\"", "");
    }

}
