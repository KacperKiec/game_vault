package io.kk.gameservice.service;

import io.kk.gameservice.exception.GameNotFoundException;
import io.kk.gameservice.model.ListType;
import io.kk.gameservice.repository.GameListRepository;
import io.kk.gameservice.util.APICallEndpoint;
import io.kk.gameservice.util.APICallParams;
import io.kk.gameservice.util.Game;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameAPIService {
    @Value("${app.rawgio-api.key}")
    private String apiKey;

    @Value("${app.rawgio-api.uri}")
    private String apiURI;

    static int pageSize = 20;

    private final ObjectMapper objectMapper;
    private final GameListRepository gameListRepository;

    private URI createURI(String endpoint, APICallParams params) {
        String uri = apiURI + endpoint + "?key=" + apiKey + params;
        return URI.create(uri);
    }

    public Game getGame(Long guid) {
        APICallParams params = new APICallParams();

        URI requestURI = createURI(
                APICallEndpoint.GAMES.label + "/" + guid,
                params
        );

        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(requestURI)
                    .GET()
                    .header("Content-Type", "application/json")
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode rootNode = objectMapper.readTree(response.body());

                List<String> genres = new ArrayList<>();
                for (JsonNode genreNode : rootNode.path("genres")) {
                    genres.add(genreNode.get("name").asText());
                }

                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
                String date = rootNode.path("released").asText();
                Date releaseDate = Objects.equals(date, "null") ? null : formatter.parse(date);

                return Game.builder()
                        .guid(guid)
                        .name(String.valueOf(rootNode.path("name")))
                        .genres(genres)
                        .backgroundImg(rootNode.path("background_image").asText())
                        .releaseDate(releaseDate)
                        .description(rootNode.path("description_raw").asText())
                        .build();
            }
        } catch (IOException | InterruptedException | ParseException e) {
            throw new RuntimeException(e);
        }

        throw new GameNotFoundException("Game not found");
    }

    public List<Game> getGames(Integer pageNumber, String nameSearch, Long userId) {
        APICallParams params = new APICallParams();
        params.addParam("page_size", String.valueOf(pageSize));

        if (pageNumber != null && pageNumber > 0) {
            params.addParam("page", String.valueOf(pageNumber));
        }

        if (nameSearch != null && !nameSearch.isEmpty()) {
            params.addParam("search", nameSearch);
        }

        URI requestURI = createURI(APICallEndpoint.GAMES.label, params);

        try (HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(requestURI)
                    .GET()
                    .header("Content-Type", "application/json")
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JsonNode rootNode = objectMapper.readTree(response.body()).path("results");

                List<Game> games = new ArrayList<>();

                for (JsonNode elemNode : rootNode) {
                    Game.GameBuilder game = Game.builder();

                    Long guid = elemNode.path("id").asLong();
                    game.guid(guid);

                    if (userId != null) {
                        var gameList = gameListRepository.findByUserIdAndGameId(userId, guid);
                        if  (gameList.isEmpty())
                            game.listType(ListType.NONE);
                        else
                            game.listType(gameList.get().getListType());
                    }

                    game.name(elemNode.get("name").asText());
                    game.backgroundImg(elemNode.get("background_image").asText());

                    List<String> genres = new ArrayList<>();
                    for (JsonNode genreNode : elemNode.path("genres")) {
                        genres.add(genreNode.get("name").asText());
                    }

                    game.genres(genres);

                    games.add(game.build());
                }

                return games;
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        throw new GameNotFoundException("Game not found");
    }
}
