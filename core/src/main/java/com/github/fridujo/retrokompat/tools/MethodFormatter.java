package com.github.fridujo.retrokompat.tools;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.StringJoiner;

/**
 * Big old copy of {@link Method#toString()} with the replacement of {@link Method#getDeclaringClass()} by the original
 * class on which the method had been scanned.
 */
public class MethodFormatter {

    private static final int ACCESS_MODIFIERS = Modifier.PUBLIC | Modifier.PROTECTED | Modifier.PRIVATE;
    public final Class<?> originalDeclaringClass;
    public final Method method;

    public MethodFormatter(Class<?> originalDeclaringClass, Method method) {
        this.originalDeclaringClass = originalDeclaringClass;
        this.method = method;
    }

    @Override
    public String toString() {
        try {
            StringBuilder sb = new StringBuilder();

            printModifiersIfNonzero(sb, Modifier.methodModifiers(), method.isDefault());
            specificToStringHeader(sb);
            sb.append('(');
            StringJoiner sj = new StringJoiner(",");
            for (Class<?> parameterType : method.getParameterTypes()) {
                sj.add(parameterType.getTypeName());
            }
            sb.append(sj);
            sb.append(')');

            if (method.getExceptionTypes().length > 0) {
                StringJoiner joiner = new StringJoiner(",", " throws ", "");
                for (Class<?> exceptionType : method.getExceptionTypes()) {
                    joiner.add(exceptionType.getTypeName());
                }
                sb.append(joiner);
            }
            return sb.toString();
        } catch (Exception e) {
            return "<" + e + ">";
        }
    }

    void printModifiersIfNonzero(StringBuilder sb, int mask, boolean isDefault) {
        int mod = method.getModifiers() & mask;

        if (mod != 0 && !isDefault) {
            sb.append(Modifier.toString(mod)).append(' ');
        } else {
            // Modifier.ACCESS_MODIFIERS
            int access_mod = mod & ACCESS_MODIFIERS;
            if (access_mod != 0)
                sb.append(Modifier.toString(access_mod)).append(' ');
            if (isDefault)
                sb.append("default ");
            mod = (mod & ~ACCESS_MODIFIERS);
            if (mod != 0)
                sb.append(Modifier.toString(mod)).append(' ');
        }
    }

    void specificToStringHeader(StringBuilder sb) {
        sb.append(method.getReturnType().getTypeName()).append(' ');
        sb.append(originalDeclaringClass.getTypeName()).append('.');
        sb.append(method.getName());
    }
}
