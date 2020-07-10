package com.github.fridujo.retrokompat;

import java.util.Set;

public interface CompatibilityError {

    class MissingType implements CompatibilityError {

        private final String typeName;

        public MissingType(String typeName) {
            this.typeName = typeName;
        }

        @Override
        public String toString() {
            return "new version removes type " + typeName;
        }
    }

    class MissingEnumValues implements CompatibilityError {

        private final String enumName;
        private final Set<String> removedValues;

        public MissingEnumValues(String enumName, Set<String> removedValues) {
            this.enumName = enumName;
            this.removedValues = removedValues;
        }

        @Override
        public String toString() {
            return "new version removes values from " + enumName + " : " + removedValues;
        }
    }

    class MissingSignatureError implements CompatibilityError {

        private final Signature unmatchedSignature;

        public MissingSignatureError(Signature unmatchedSignature) {
            this.unmatchedSignature = unmatchedSignature;
        }

        @Override
        public String toString() {
            return "new version is missing " + unmatchedSignature;
        }
    }

    class AmbiguousSignatureError implements CompatibilityError {

        private final Signature v1Signature;
        private final Set<Signature> matchedV2Signatures;

        public AmbiguousSignatureError(Signature v1Signature, Set<Signature> matchedV2Signatures) {
            this.v1Signature = v1Signature;
            this.matchedV2Signatures = matchedV2Signatures;
        }

        @Override
        public String toString() {
            return "new version adds ambiguity for " + v1Signature;
        }
    }


}
