package com.github.fridujo.retrokompat;

import java.util.HashSet;
import java.util.Set;

public class JavaType {

    public final Class<?> type;

    public JavaType(Class<?> type) {
        this.type = type;
    }

    public static Set<String> listTypesInHierarchy(Class<?> type) {
        return listTypesInHierarchy(type, new HashSet<>());
    }

    private static Set<String> listTypesInHierarchy(Class<?> type, Set<Class<?>> visited) {
        Set<String> hierarchy = new HashSet<>();
        if (!visited.contains(type)) {
            visited.add(type);
            if (type.isInterface()) {
                // an instance of a class implementing this interface IS an Object
                hierarchy.add(Object.class.getName());
            }
            Class<?> superclass = type.getSuperclass();
            if (superclass != null) {
                hierarchy.add(superclass.getName());
                hierarchy.addAll(listTypesInHierarchy(superclass, visited));
            }
            for (Class<?> interf : type.getInterfaces()) {
                hierarchy.add(interf.getName());
                hierarchy.addAll(listTypesInHierarchy(interf, visited));
            }
        }
        return hierarchy;
    }

    /**
     * @return true if the current {@link JavaType} is a an interface implemented or a class extended by the given one
     */
    public boolean isLessSpecific(JavaType other) {
        if (type.getName().equals(other.type.getName())) {
            return false;
        }
        Set<String> upperTypes = listTypesInHierarchy(other.type);
        return upperTypes.contains(type.getName());
    }

    public boolean isSame(JavaType other) {
        return type.getName().equals(other.type.getName());
    }
}
