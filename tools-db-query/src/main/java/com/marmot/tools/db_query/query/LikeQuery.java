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
public class LikeQuery extends AbstractOppositionQuery {
    private String likeValue;

    //true：左模糊匹配
    private boolean left;

    //true:右模糊匹配
    private boolean right;

    public LikeQuery(String field, String likeValue, boolean left, boolean right, boolean opposition) {
        this.setType(QueryTypeEnum.LIKE.getName());
        this.setField(field);
        this.setLikeValue(likeValue);
        this.setLeft(left);
        this.setRight(right);
        this.setOpposition(opposition);
    }
}
