package io.kk.gameservice.exception;

public class GameApiException extends RuntimeException {
    public GameApiException(String message) {
        super(message);
    }
}
