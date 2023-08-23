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
public class NullQuery extends AbstractOppositionQuery {

    public NullQuery(String field, boolean opposition) {
        this.setType(QueryTypeEnum.NULL.getName());
        this.setField(field);
        this.setOpposition(opposition);
    }
}
