package com.github;

public class WrongFoodTypeException extends RuntimeException {

    WrongFoodTypeException(String message) {
        super(message);
    }
}
