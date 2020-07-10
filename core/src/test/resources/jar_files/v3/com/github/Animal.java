package com.github;

public interface Animal {

    void receiveFood(FoodType type) throws WrongFoodTypeException;

    /**
     * Adding new method is backward compatible.
     */
    void putInABox();
}
