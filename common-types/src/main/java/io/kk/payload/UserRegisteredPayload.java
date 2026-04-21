package io.kk.payload;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class UserRegisteredPayload {
    private String username;
    private String email;
}
