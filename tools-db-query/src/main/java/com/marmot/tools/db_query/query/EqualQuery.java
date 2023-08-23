package com.marmot.tools.db_query.query;

import com.marmot.tools.db_query.enums.QueryTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @Author:zhaozhou
 * @Date: 2023/07/18
 * @Desc:
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper=false)
public class EqualQuery extends AbstractOppositionQuery {
    private String value;


    public EqualQuery(String field, String value, boolean opposition) {
        this.setType(QueryTypeEnum.EQUAL.getName());
        this.setField(field);
        this.setValue(value);
        this.setOpposition(opposition);
    }
}
