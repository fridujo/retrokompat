package com.github.fridujo.retrokompat;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class Exceptions {

    private final Class<?>[] types;

    public Exceptions(Class<?>... types) {
        this.types = types;
    }

    public boolean areTheSame(Exceptions others) {
        if (types.length != others.types.length) {
            return false;
        }

        Set<String> typesAsString = Arrays.stream(types).map(Class::getName).collect(Collectors.toSet());

        return Arrays.stream(others.types).map(Class::getName).noneMatch(t -> !typesAsString.contains(t));
    }
}
