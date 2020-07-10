package com.github;

public class Cat implements Animal {

    @Override
    public void pet() throws NotPettableException {
    }

    @Override
    public void receiveFood(FoodType type) throws WrongFoodTypeException {
        if (type != FoodType.COOKED_MEAT) {
            throw new WrongFoodTypeException("yeks");
        }
    }

    public void receiveFood(FoodType type, String foodName) throws WrongFoodTypeException {
        receiveFood(type);
    }

    @Override
    public void putInABox() {
    }

    public String doThat(String action) {
        return "no";
    }
}
