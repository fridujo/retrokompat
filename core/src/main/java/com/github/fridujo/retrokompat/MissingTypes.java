package com.github.fridujo.retrokompat;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

class MissingTypes {

    final Set<String> missingTypes;

    MissingTypes(JarObject v1Jar, JarObject v2Jar) {
        Set<String> v2Classes = v2Jar.streamPublicTypes().map(Class::getName).collect(Collectors.toSet());

        this.missingTypes = v1Jar.streamPublicTypes()
            .map(Class::getName)
            .filter(v1Class -> !v2Classes.contains(v1Class))
            .collect(Collectors.toSet());
    }

    Set<CompatibilityError> toErrors() {
        return missingTypes.stream().map(CompatibilityError.MissingType::new).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public boolean contains(Class<?> declaringClass) {
        return missingTypes.contains(declaringClass.getName());
    }
}
