package io.github.zhaozhou1489.tools_db_query.query;

import io.github.zhaozhou1489.tools_db_query.enums.QueryTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Author:zhaozhou
 * @Date: 2023/07/18
 * @Desc:
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper=false)
public class InQuery extends AbstractOppositionQuery {
    private List<String> values;

    public InQuery(String field, List<String> values, boolean opposition) {
        this.setType(QueryTypeEnum.IN.getName());
        this.setField(field);
        this.setValues(values);
        this.setOpposition(opposition);
    }
}
