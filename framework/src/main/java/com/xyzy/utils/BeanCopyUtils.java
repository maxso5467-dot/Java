package com.xyzy.utils;

import org.springframework.beans.BeanUtils;

import java.util.List;
import java.util.stream.Collectors;

public class BeanCopyUtils {
    private BeanCopyUtils() {}

    public static <T> T copyBean(Object source, Class<T> clazz) {
        T result = null;
        try {
            result = clazz.getDeclaredConstructor().newInstance();
            BeanUtils.copyProperties(source, result);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    public static <T> List<T> copyBeanList(List<?> list, Class<T> clazz) {
        return list.stream()
                .map(o -> copyBean(o, clazz))
                .collect(Collectors.toList());
    }
}
