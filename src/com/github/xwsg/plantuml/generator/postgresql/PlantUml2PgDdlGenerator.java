package com.github.xwsg.plantuml.generator.postgresql;

import com.github.xwsg.plantuml.generator.AbstractDdlGenerator;
import com.github.xwsg.plantuml.model.Column;
import com.github.xwsg.plantuml.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * PostgreSQL DDL generator.
 *
 * @author xwsg
 */
public class PlantUml2PgDdlGenerator extends AbstractDdlGenerator {

    @Override
    protected String genDdlText() {
        StringBuilder ddlSb = new StringBuilder();
        tables.forEach(tbl -> {
            ddlSb.append("create table ").append(quote(tbl.getName())).append(" (");
            int lineNo = 0;
            List<Column> commentColumns = new ArrayList<>();
            for (Column clm : tbl.getColumns()) {
                if (lineNo != 0) {
                    ddlSb.append(",");
                }
                lineNo++;
                ddlSb.append(LINE_SEPARATOR).append(COLUMN_INDENT);
                ddlSb.append(quote(clm.getName())).append(" ").append(clm.getDataType());
                if (StringUtils.isNotEmpty(clm.getDefaultValue())) {
                    ddlSb.append(" default ").append(clm.getDefaultValue());
                }
                if (clm.isNotNull() || clm.isPrimaryKey()) {
                    ddlSb.append(" not null");
                }
                if (clm.isPrimaryKey()) {
                    ddlSb.append(" primary key");
                }
                if (StringUtils.isNotEmpty(clm.getComment())) {
                    commentColumns.add(clm);
                }
            }
            ddlSb.append(LINE_SEPARATOR).append(");").append(LINE_SEPARATOR);
            ddlSb.append("comment on table ").append(quote(tbl.getName())).append(" is '")
                    .append(tbl.getComment()).append("';").append(LINE_SEPARATOR);
            commentColumns.forEach(commentColumn -> {
                ddlSb.append("comment on column ").append(quote(tbl.getName())).append(".")
                        .append(quote(commentColumn.getName())).append(" is '")
                        .append(commentColumn.getComment()).append("';")
                        .append(LINE_SEPARATOR);
            });
            ddlSb.append(LINE_SEPARATOR);
        });
        return ddlSb.toString();
    }

    @Override
    protected String quote(String str) {
        return "\"" + str + "\"";
    }
}
