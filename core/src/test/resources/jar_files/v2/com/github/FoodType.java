package com.github;

public enum FoodType {
    RAW_MEAT,
    /**
     * Removing values from enum is NOT backward compatible.
     */
    COOKED_MEAT,
    /**
     * Adding values to enum is backward compatible.
     */
    VEGETABLES;
}
