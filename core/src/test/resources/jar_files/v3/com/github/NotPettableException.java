package com.github;

/**
 * Make exceptions checked is NOT backward compatible.
 */
public class NotPettableException extends Exception {

    NotPettableException(String message) {
        super(message);
    }
}
