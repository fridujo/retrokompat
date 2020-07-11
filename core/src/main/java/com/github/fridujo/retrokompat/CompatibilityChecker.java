package com.github.fridujo.retrokompat;

import static java.util.Collections.emptySet;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class CompatibilityChecker {

    public Set<CompatibilityError> check(Path v1JarPath, Path v2JarPath) {
        return check(v1JarPath, emptySet(), v2JarPath, emptySet());
    }

    public Set<CompatibilityError> check(Path v1JarPath, Set<Path> v1DependencyPaths, Path v2JarPath, Set<Path> v2DependencyPaths) {
        JarObject v1Jar = new JarObject(v1JarPath, v1DependencyPaths);
        JarObject v2Jar = new JarObject(v2JarPath, v2DependencyPaths);

        MissingTypes missingTypes = new MissingTypes(v1Jar, v2Jar);

        Set<CompatibilityError> errors = new LinkedHashSet<>();

        errors.addAll(missingTypes.toErrors());
        errors.addAll(checkEnumCompatibility(v1Jar, v2Jar));
        errors.addAll(checkExecutableCompatibility(v1Jar, v2Jar, missingTypes));

        return errors;
    }

    private Set<CompatibilityError> checkEnumCompatibility(JarObject v1Jar, JarObject v2Jar) {
        final Map<String, Set<String>> valuesByEnumName = v2Jar.streamPublicTypes()
            .filter(c -> c.isEnum())
            .collect(Collectors.toMap(
                c -> c.getName(),
                c -> valueNames(c)
            ));
        return v1Jar.streamPublicTypes()
            .filter(c -> c.isEnum())
            .map(ec -> {
                Set<String> v2Values = valuesByEnumName.get(ec.getName());
                if (v2Values == null) {
                    // Missing types are handled outside
                    return null;
                }
                Set<String> removedValues = valueNames(ec);
                removedValues.removeAll(v2Values);

                if (!removedValues.isEmpty()) {
                    return new CompatibilityError.MissingEnumValues(ec.getName(), removedValues);
                } else {
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
    }

    private Set<String> valueNames(Class<?> c) {
        return Arrays.stream(c.getEnumConstants()).map(Enum.class::cast).map(t -> t.name()).collect(Collectors.toSet());
    }

    private Set<CompatibilityError> checkExecutableCompatibility(JarObject v1Jar, JarObject v2Jar, MissingTypes missingTypes) {
        Set<Signature> v1Signatures = v1Jar.extractSignatures().stream()
            .filter(s -> !missingTypes.contains(s.executable.getDeclaringClass()))
            .collect(Collectors.toCollection(LinkedHashSet::new));
        Set<Signature> v2Signatures = v2Jar.extractSignatures();

        Set<CompatibilityError> errors = new LinkedHashSet<>();
        for (Signature v1Signature : v1Signatures) {
            Set<Signature> matchedSignatures = matchingSignatures(v1Signature, v2Signatures);
            if (matchedSignatures.isEmpty()) {
                errors.add(new CompatibilityError.MissingSignatureError(v1Signature));
            } else if (matchedSignatures.size() > 1) {
                errors.add(new CompatibilityError.AmbiguousSignatureError(v1Signature, matchedSignatures));
            }
        }
        return errors;
    }

    private Set<Signature> matchingSignatures(Signature v1Signature, Set<Signature> v2Signatures) {
        return v2Signatures.stream()
            .filter(v1Signature::isCompatibleWith)
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
