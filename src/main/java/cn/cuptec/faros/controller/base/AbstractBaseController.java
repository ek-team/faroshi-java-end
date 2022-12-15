package cn.cuptec.faros.controller.base;

import cn.cuptec.faros.common.annotation.Queryable;
import cn.cuptec.faros.common.constrants.CommonConstants;
import cn.cuptec.faros.common.enums.QueryLogical;
import cn.cuptec.faros.common.utils.CamelUtils;
import cn.cuptec.faros.common.utils.StringUtils;
import cn.cuptec.faros.common.utils.http.ServletUtils;
import cn.cuptec.faros.entity.User;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ReflectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;

import java.beans.PropertyEditorSupport;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public abstract class AbstractBaseController<T extends ServiceImpl, E> {
    private Logger log = LoggerFactory.getLogger(AbstractBaseController.class);

    @Autowired
    public T service;

    protected static final String DATA_QUERY_SUCCESS = "查询成功";
    protected static final String DATA_QUERY_FAILED = "数据不存在";
    protected static final String DATA_INSERT_SUCCESS = "入库成功";
    protected static final String DATA_INSERT_FAILED = "入库失败";
    protected static final String DATA_DELETE_SUCCESS = "删除成功";
    protected static final String DATA_DELETE_FAILED = "数据不存在或已被删除";
    protected static final String DATA_UPDATE_SUCCESS = "更改成功";
    protected static final String DATA_UPDATE_FAILED = "更改失败,请确认数据存在";

    /**
     * 将前台传递过来的日期格式的字符串，自动转化为Date类型
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        // Date 类型转换
        binder.registerCustomEditor(Date.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                if (StringUtils.isEmpty(text))
                    setValue(null);
                else
                    setValue(DateUtil.parseDate(text));
            }
        });
    }

    //region protected

    protected abstract Class<E> getEntityClass();

    protected Page emptyPage() {
        Page page = new Page();
        page.setCurrent(1L);
        page.setSize(0);
        page.setRecords(new ArrayList());
        return page;
    }

    /**
     * 获取mybatis-plus的Page分页实体参数
     *
     * @return
     */
    protected Page<E> getPage() {
        Page<E> page = new Page<>();
        return getPage(page);
    }

    protected Page getPage(Page page) {
        Integer size = ServletUtils.getParameterToInt(CommonConstants.PAGE_SIZE);
        Integer current = ServletUtils.getParameterToInt(CommonConstants.PAGE_NUM);
        if (size == null) size = 10;
        if (current == null) current = 1;
        if (size != null && current != null) {
            if (size <= 0)
                size = 10;
            if (current <= 0)
                current = 1;
            page.setSize(size);
            page.setCurrent(current);
        } else {
            return null;
        }
        String[] ascs = ServletUtils.getParameterToStringArray(CommonConstants.ASCS);
        if (StringUtils.isNotEmpty(ascs)) {
            List<String> ascColumns = new ArrayList<>();
            for (String ascColumn : ascs) {
                ascColumns.add(CamelUtils.camelToUnderline(ascColumn));
            }
            page.setAscs(ascColumns);
        }
        String[] descs = ServletUtils.getParameterToStringArray(CommonConstants.DESCS);
        if (StringUtils.isNotEmpty(descs)) {
            List<String> descColumns = new ArrayList<>();
            for (String descColumn : descs) {
                descColumns.add(CamelUtils.camelToUnderline(descColumn));
            }
            page.setDescs(descColumns);
        }
        return page;
    }

    /**
     * 获取queryWrapper
     *
     * @return
     */
    protected QueryWrapper<E> getQueryWrapper(Class clazz) {
        QueryWrapper queryWrapper = new QueryWrapper<User>();
        Field[] fields = ReflectUtil.getFields(clazz);
        for (Field field : fields) {
            //如果注解不为空，根据注解及参数设置查询参数
            String name = CamelUtils.camelToUnderline(clazz.getSimpleName());
            if (!StringUtils.isEmpty(name)) {
                name = name.toLowerCase();
            }
            queryWrapper = refectQueryWrapperByQueryable(queryWrapper, field, name);
        }
        return queryWrapper;
    }

    /**
     * 根据Queryable注解设置queryWrapper的查询条件
     *
     * @param queryWrapper
     * @param field
     * @return
     */
    private QueryWrapper<E> refectQueryWrapperByQueryable(QueryWrapper<E> queryWrapper, Field field, String tableName) {
        Queryable queryable = field.getAnnotation(Queryable.class);
        if (queryable == null)
            return queryWrapper;
        String columnName = field.getName(); //列名
        if (queryable.queryLogical() != QueryLogical.QUANTUM && queryable.queryLogical() != QueryLogical.BATCH_OR) {                             //查询逻辑如果不为QUANTUM,查询参数应该为字段名，否则应该为“字段名+Begin”和“字段名+End”
            String queryParameter = ServletUtils.getParameter(field.getName());             //此条件下查询参数的名称应该为字段名
            if (StringUtils.isEmpty(queryParameter))                                                     //参数不传，即为空时，不设置此字段的查询条件
                return queryWrapper;
            else {
                if (StringUtils.isNotEmpty(queryable.columnName())) {
                    columnName = queryable.columnName();
                } else {
                    columnName = tableName + "." + CamelUtils.camelToUnderline(columnName);
                }
                switch (queryable.queryLogical()) {
                    case EQUAL: {
                        if (queryParameter.toLowerCase().equals("null")) {   //如果查询参数为“null”，则此查询参数为空
                            queryWrapper.isNull(columnName);
                        } else {
                            queryWrapper.eq(columnName, queryParameter);
                        }
                        break;
                    }
                    case GE: {
                        if (StringUtils.isNotEmpty(queryParameter))
                            queryWrapper.ge(columnName, queryParameter);
                        break;
                    }
                    case GT: {
                        if (StringUtils.isNotEmpty(queryParameter))
                            queryWrapper.gt(columnName, queryParameter);
                        break;
                    }
                    case LE: {
                        if (StringUtils.isNotEmpty(queryParameter))
                            queryWrapper.le(columnName, queryParameter);
                        break;
                    }
                    case LT: {
                        if (StringUtils.isNotEmpty(queryParameter))
                            queryWrapper.lt(columnName, queryParameter);
                        break;
                    }
                    case LIKE: {
                        if (StringUtils.isNotEmpty(queryParameter))
                            queryWrapper.like(columnName, queryParameter);
                        break;
                    }
                }
            }
        } else if (queryable.queryLogical() == QueryLogical.QUANTUM) {
            String timeQuantumBegin = ServletUtils.getParameter(field.getName() + "Begin");
            String timeQuantumEnd = ServletUtils.getParameter(field.getName() + "End");
            if (StringUtils.isNotEmpty(timeQuantumBegin)) {
                queryWrapper.ge(columnName, timeQuantumBegin);
            }
            if (StringUtils.isNotEmpty(timeQuantumEnd)) {
                queryWrapper.le(columnName, timeQuantumEnd);
            }
        } else if (queryable.queryLogical() == QueryLogical.BATCH_OR) {
            String origionalParameter = ServletUtils.getParameter(field.getName());
            if (StringUtils.isNotEmpty(origionalParameter)) {
                String[] parameterArray = origionalParameter.split(CommonConstants.COMMA);
                if (parameterArray != null && parameterArray.length > 0) {

                    boolean isAllEmpty = true;
                    for (String parameter :
                            parameterArray) {
                        if (StringUtils.isNotEmpty(parameter)) {
                            isAllEmpty = false;
                            break;
                        }
                    }
                    if (isAllEmpty && !field.getDeclaringClass().isAssignableFrom(String.class)) {

                    } else {
                        queryWrapper.and(wrapper -> {
                            int tempIndex = 0;
                            if (!field.getDeclaringClass().isAssignableFrom(String.class)) {
                                for (; tempIndex < parameterArray.length; tempIndex++) {
                                    if (StringUtils.isNotEmpty(parameterArray[tempIndex])) {
                                        wrapper.eq(field.getName(), parameterArray[tempIndex]);
                                        break;
                                    }
                                }
                            } else {
                                wrapper.eq(field.getName(), parameterArray[tempIndex]);
                            }
                            tempIndex = tempIndex + 1;
                            for (; tempIndex < parameterArray.length; tempIndex++) {
                                wrapper.or().eq(field.getName(), parameterArray[tempIndex]);
                            }
                            return wrapper;
                        });
                    }
                }
            }
        }
        return queryWrapper;
    }

    public static IPage getNullablePage() {
        Integer size = ServletUtils.getParameterToInt(CommonConstants.PAGE_SIZE);
        Integer current = ServletUtils.getParameterToInt(CommonConstants.PAGE_NUM);
        if (size != null && current != null) {
            IPage page = new Page();
            if (size <= 0)
                size = 10;
            if (current <= 0)
                current = 1;
            page.setSize(size);
            page.setCurrent(current);
            return page;
        } else {
            return null;
        }
    }

}