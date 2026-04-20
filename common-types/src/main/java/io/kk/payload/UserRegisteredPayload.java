package io.kk.payload;

import lombok.Builder;

@Builder
public class UserRegisteredPayload {
    private String username;
    private String email;
}
