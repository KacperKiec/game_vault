package io.kk.gameservice.util;

public enum APICallEndpoint {
    GAMES("/games"),
    GAME("/game"),
    GENRES("/genres"),
    PLATFORMS("/platforms"),;

    public final String label;

    APICallEndpoint(String label) {
        this.label = label;
    }
}
