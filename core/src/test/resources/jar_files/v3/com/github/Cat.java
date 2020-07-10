package com.github;

public class Cat implements Animal {

    @Override
    public void receiveFood(FoodType type) throws WrongFoodTypeException {
        if (type != FoodType.VEGETABLES) {
            throw new WrongFoodTypeException("yeks");
        }
    }

    /**
     * Reducing visibility is NOT backward compatible.
     */
    void receiveFood(FoodType type, String foodName) throws WrongFoodTypeException {
        receiveFood(type);
    }

    @Override
    public void putInABox() {
    }

    public String doThat(String action) {
        return "no";
    }

    public String doThat(Object action) {
        return "no";
    }
}
