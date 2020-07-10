package com.github;

/**
 * Removing package-protected class is backward compatible.
 */
class Lion implements Animal {

    @Override
    public void pet() throws NotPettableException {
        throw new NotPettableException("grrrr");
    }

    @Override
    public void receiveFood(FoodType type) throws WrongFoodTypeException {
        if (type != FoodType.RAW_MEAT) {
            throw new WrongFoodTypeException("yeks");
        }
    }
}
