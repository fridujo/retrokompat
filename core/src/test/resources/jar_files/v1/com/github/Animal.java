package com.github;

public interface Animal {

    void pet() throws NotPettableException;

    void receiveFood(FoodType type) throws WrongFoodTypeException;
}
