package com.github.allduke.dancingquery.base;

import com.github.allduke.dancingquery.exception.FieldHandleException;
import com.github.allduke.dancingquery.exception.InvalidConditionException;
import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
import com.googlecode.concurrentlinkedhashmap.Weighers;
import lombok.Getter;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.*;

/**
 * @author: allenduke
 * @mail: allenduke@163.com
 * @date 2021/12/27
 * description:
 */
@Getter
public class Query<T> implements Serializable {

    private static String EMPTY_FIELD_NAME = "";

    private static final Map<SqlFunction, Field> SQL_FUNCTION_FIELD_NAME_MAP = new ConcurrentLinkedHashMap.Builder<SqlFunction, Field>()
            .maximumWeightedCapacity(2000).weigher(Weighers.singleton()).build();

    private static <T> Field getField(SqlFunction<T, ?> fn) {
        Field field = SQL_FUNCTION_FIELD_NAME_MAP.get(fn);
        if (field == null) {
            // 从function取出序列化方法
            Method writeReplaceMethod;
            try {
                writeReplaceMethod = fn.getClass().getDeclaredMethod("writeReplace");
            } catch (NoSuchMethodException e) {
                throw new FieldHandleException(e);
            }

            // 从序列化方法取出序列化的lambda信息
            writeReplaceMethod.setAccessible(true);
            SerializedLambda serializedLambda;
            try {
                serializedLambda = (SerializedLambda) writeReplaceMethod.invoke(fn);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new FieldHandleException(e);
            }

            // 从lambda信息取出method、field、class等
            try {
                field = resolveField(serializedLambda.getCapturingClass(), serializedLambda.getImplMethodName());
            } catch (ClassNotFoundException | NoSuchFieldException e) {
                throw new FieldHandleException(e);
            }
            SQL_FUNCTION_FIELD_NAME_MAP.put(fn, field);
        }
        return field;
    }

    private static Field resolveField(String className, String methodName) throws ClassNotFoundException, NoSuchFieldException {
        String fieldNameSub;
        if (methodName.startsWith("get")) {
            fieldNameSub = methodName.substring(3);
        } else if (methodName.startsWith("is")) {
            fieldNameSub = methodName.substring(2);
        } else {
            throw new FieldHandleException("cant resolve field from method:" + methodName);
        }

        String fieldName = fieldNameSub.replaceFirst(fieldNameSub.charAt(0) + "", (fieldNameSub.charAt(0) + "").toLowerCase());
        className = className.replace(java.io.File.separator, ".");
        return Class.forName(className).getDeclaredField(fieldName);
    }

    /**
     * 为null则默认搜索全部字段。
     */
    private List<String> wantFieldList;

    private List<Condition> conditions = new ArrayList<>();

    /**
     * 默认更新时间倒序。
     */
    private List<Order> orders;

    private void addCondition(SqlFunction<T, ?> fn, Object val, Integer connectType, Integer conditionType) {
        if (val == null) {
            throw new InvalidConditionException("val can not be null.");
        }
        Type type = null;
        if (val instanceof Collection) {
            Collection<?> c = (Collection<?>) val;
            if (c.isEmpty()) {
                throw new InvalidConditionException("collection can not be empty.");
            }
            for (Object o : c) {
                if (o == null) {
                    throw new InvalidConditionException("ele in collection can not be null.");
                }
                if (type == null) {
                    type = o.getClass();
                    continue;
                }
                if (!type.equals(o.getClass())) {
                    throw new InvalidConditionException("ele's type in collection can not be different.");
                }
            }
        } else {
            type = val.getClass();
        }
        Field field = getField(fn);
        if (!field.getType().equals(type)) {
            throw new InvalidConditionException("field's type must be equals val's type.");
        }
        Condition condition = new Condition(connectType, conditionType, field, val);
        conditions.add(condition);
    }

    public Query<T> select(SqlFunction<T, ?> fn) {
        Field field = getField(fn);
        if (wantFieldList == null) {
            wantFieldList = new ArrayList<>();
        }
        wantFieldList.add(field.getName());
        return this;
    }

    public Query<T> eq(SqlFunction<T, ?> fn, Object val) {
        addCondition(fn, val, Condition.CONNECT_TYPE_AND, Condition.CONDITION_TYPE_EQ);
        return this;
    }

    public Query<T> ne(SqlFunction<T, ?> fn, Object val) {
        addCondition(fn, val, Condition.CONNECT_TYPE_AND, Condition.CONDITION_TYPE_NOT_EQ);
        return this;
    }

    public Query<T> in(SqlFunction<T, ?> fn, Collection<?> val) {
        addCondition(fn, val, Condition.CONNECT_TYPE_AND, Condition.CONDITION_TYPE_IN);
        return this;
    }

    public Query<T> notIn(SqlFunction<T, ?> fn, Collection<?> val) {
        addCondition(fn, val, Condition.CONNECT_TYPE_AND, Condition.CONDITION_TYPE_NOT_IN);
        return this;
    }

    public Query<T> like(SqlFunction<T, ?> fn, Object val) {
        addCondition(fn, val, Condition.CONNECT_TYPE_AND, Condition.CONDITION_TYPE_LIKE);
        return this;
    }

    public Query<T> notLike(SqlFunction<T, ?> fn, Object val) {
        addCondition(fn, val, Condition.CONNECT_TYPE_AND, Condition.CONDITION_TYPE_NOT_LIKE);
        return this;
    }

    private void addOrder(SqlFunction<T, ?> fn, Boolean isAsc) {
        Field field = getField(fn);
        Order order = new Order(field.getName(), isAsc);
        if (orders == null) {
            orders = new ArrayList<>();
        }
        orders.add(order);
    }

    public Query<T> orderByAsc(SqlFunction<T, ?> fn) {
        addOrder(fn, true);
        return this;
    }

    public Query<T> orderByDesc(SqlFunction<T, ?> fn) {
        addOrder(fn, false);
        return this;
    }

    public void validate() {

    }
}
