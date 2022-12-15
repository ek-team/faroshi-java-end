package cn.cuptec.faros.common.enums;

/**
 * Creater: Miao
 * CreateTime: 2019/4/11 10:34
 * Description: 查询条件的逻辑
 */
public enum QueryLogical {
    /**
     * 等于
     */
    EQUAL,

    /**
     * 大于
     */
    GT,

    /**
     * 大于等于
     */
    GE,

    /**
     * 小于
     */
    LT,

    /**
     * 小于等于
     */
    LE,

    /**
     * 时间段
     * 当Queryable的查询逻辑为TIME_QUANTUM时，对应的参数为“字段名 + Begin“ 和 ”字段名 + End“
     */
    QUANTUM,

    /**
     * 模糊查询
     */
    LIKE,

    /**
     * 批量或
     */
    BATCH_OR,

}
