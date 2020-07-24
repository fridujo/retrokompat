package com.github.fridujo.retrokompat;

import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.Arrays;

import com.github.fridujo.retrokompat.tools.MethodFormatter;

public class Signature {

    public final Class<?> originalDeclaringClass;
    public final Executable executable;
    private final boolean isMethod;

    public Signature(Executable executable) {
        this(executable.getDeclaringClass(), executable);
    }

    public Signature(Class<?> originalDeclaringClass, Executable executable) {
        this.originalDeclaringClass = originalDeclaringClass;
        this.executable = executable;
        this.isMethod = executable instanceof Method;
    }

    @Override
    public String toString() {
        return isMethod ? new MethodFormatter(originalDeclaringClass, (Method) executable).toString() : executable.toString();
    }

    /**
     * No direct comparison must be made between objects from {@link java.lang.reflect} as they are loaded from
     * separated {@link ClassLoader}s.
     */
    public boolean isCompatibleWith(Signature v2) {
        if (!executable.getDeclaringClass().getName().equals(v2.executable.getDeclaringClass().getName())) {
            return false;
        }
        if (!executable.getName().equals(v2.executable.getName())) {
            return false;
        }
        if (executable.getParameterCount() != v2.executable.getParameterCount()) {
            return false;
        }
        if (isMethod && v2.getReturnType().isLessSpecific(getReturnType())) {
            return false;
        }
        for (int paramIndex = 0; paramIndex < executable.getParameterCount(); paramIndex++) {
            if (!getParameterType(paramIndex).isSame(v2.getParameterType(paramIndex))) {
                return false;
            }
        }

        if (!getCheckedExceptionTypes().areTheSame(v2.getCheckedExceptionTypes())) {
            return false;
        }

        return true;
    }

    private JavaType getReturnType() {
        return isMethod ? new JavaType(((Method) executable).getReturnType()) : null;
    }

    private JavaType getParameterType(int paramIndex) {
        return new JavaType(executable.getParameterTypes()[paramIndex]);
    }

    private Exceptions getCheckedExceptionTypes() {
        return new Exceptions(Arrays
            .stream(executable.getExceptionTypes())
            .filter(c -> !RuntimeException.class.isAssignableFrom(c))
            .filter(c -> !Error.class.isAssignableFrom(c))
            .toArray(Class<?>[]::new)
        );
    }
}
