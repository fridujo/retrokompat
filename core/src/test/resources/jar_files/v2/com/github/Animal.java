package com.github;

public interface Animal {

    /**
     * Removing a public method is NOT backward compatible.
     */
    void pet() throws NotPettableException;

    void receiveFood(FoodType type) throws WrongFoodTypeException;

    /**
     * Adding a public method is backward compatible.
     */
    void putInABox();
}
