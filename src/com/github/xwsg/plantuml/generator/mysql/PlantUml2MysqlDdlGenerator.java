package com.github.xwsg.plantuml.generator.mysql;

import com.github.xwsg.plantuml.generator.AbstractDdlGenerator;
import com.github.xwsg.plantuml.model.Column;
import com.github.xwsg.plantuml.util.StringUtils;

/**
 * MySQL DDL generator.
 *
 * @author xwsg
 */
public class PlantUml2MysqlDdlGenerator extends AbstractDdlGenerator {

    @Override
    protected String genDdlText() {
        StringBuilder ddlSb = new StringBuilder();
        tables.forEach(tbl -> {
            ddlSb.append("create table ").append(quote(tbl.getName())).append(" (").append(LINE_SEPARATOR);
            // only support first primary key column
            String pkClmName = null;
            int lineNo = 0;
            for (Column clm : tbl.getColumns()) {
                if (lineNo != 0) {
                    ddlSb.append(",").append(LINE_SEPARATOR);
                }
                lineNo++;
                ddlSb.append(COLUMN_INDENT);
                ddlSb.append(quote(clm.getName())).append(" ").append(clm.getDataType());
                if (clm.isNotNull() || clm.isPrimaryKey()) {
                    ddlSb.append(" not null");
                }
                if (StringUtils.isNotEmpty(clm.getDefaultValue()) ) {
                    ddlSb.append(" default ").append(clm.getDefaultValue());
                }
                if (clm.isAutoInc()) {
                    ddlSb.append(" auto_increment");
                }
                if (StringUtils.isNotEmpty(clm.getComment())) {
                    ddlSb.append(" comment '").append(clm.getComment()).append("'");
                }
                if (clm.isPrimaryKey() && StringUtils.isEmpty(pkClmName)) {
                    pkClmName = clm.getName();
                }
            }
            if (StringUtils.isNotEmpty(pkClmName)) {
                ddlSb.append(",").append(LINE_SEPARATOR);
                ddlSb.append(COLUMN_INDENT).append("primary key (").append(quote(pkClmName)).append(")");
                ddlSb.append(LINE_SEPARATOR);
            }
            ddlSb.append(")");
            if (StringUtils.isNotEmpty(tbl.getComment())) {
                ddlSb.append(" comment '").append(tbl.getComment()).append("';");
            }
            ddlSb.append(LINE_SEPARATOR).append(LINE_SEPARATOR);
        });
        return ddlSb.toString();
    }

    @Override
    protected String quote(String str) {
        return "`" + str + "`";
    }
}
