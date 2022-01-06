package com.github.allduke.dancingquery.base;

import java.io.Serializable;
import java.util.function.Function;

/**
 * @author: allenduke
 * @mail: allenduke@163.com
 * @date 2021/12/27
 * description:
 */
public interface SqlFunction<T, R> extends Function<T, R>, Serializable {
}
