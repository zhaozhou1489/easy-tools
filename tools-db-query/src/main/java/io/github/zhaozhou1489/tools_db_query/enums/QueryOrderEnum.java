package io.github.zhaozhou1489.tools_db_query.enums;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

public enum QueryOrderEnum {
    DESC("DESC"),
    ASC("ASC"),
    ;

    private String orderStr;

    public String getOrderStr() {
        return orderStr;
    }

    QueryOrderEnum(String orderStr) {
        this.orderStr = orderStr;
    }

    public static QueryOrderEnum of(String orderStr) {
        return StringUtils.isBlank(orderStr) ? DESC : Arrays.stream(QueryOrderEnum.values()).filter(str -> str.getOrderStr().equals(orderStr)).findFirst().orElse(DESC);
    }
}
