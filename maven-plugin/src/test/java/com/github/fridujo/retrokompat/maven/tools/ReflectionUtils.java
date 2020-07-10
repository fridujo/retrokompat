package com.github.fridujo.retrokompat.maven.tools;

import java.lang.reflect.Field;

public class ReflectionUtils {

    public static void setField(Object object, String fieldName, Object fieldValue) {
        try {
            Field field = object.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(object, fieldValue);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
