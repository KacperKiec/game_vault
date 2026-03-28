package io.kk.gameservice.service;

import io.kk.gameservice.dto.UserDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InternalServiceClient {

    private final WebClient webClient;

    @Value("${app.user-service-url}")
    private String userServiceUrl;

    public boolean checkIfUserExists(Long userId) {
        return Boolean.TRUE.equals(webClient.get()
                .uri(userServiceUrl + "/internal/user/{id}", userId)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block());
    }

    public List<UserDTO> getUsernames(List<Long> ids) {
        return webClient.get()
                .uri(userServiceUrl + "/internal/usernames",
                        uri -> uri.queryParam("ids", ids)
                                .build())
                .retrieve()
                .bodyToFlux(UserDTO.class)
                .collectList()
                .block();
    }
}
