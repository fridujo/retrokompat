package com.github;

/**
 * Adding a public type is backward compatible.
 * Removing a public type is NOT backward compatible.
 */
public class Dog implements Animal {

    @Override
    public void pet() throws NotPettableException {
    }

    @Override
    public void receiveFood(FoodType type) throws WrongFoodTypeException {
    }

    @Override
    public void putInABox() {
    }
}
