package io.kk.gameservice.service;

import io.kk.gameservice.dto.GameDTO;
import io.kk.gameservice.dto.GameDetailsDTO;
import io.kk.gameservice.dto.GameParamsDTO;
import io.kk.gameservice.dto.ParamDTO;
import io.kk.gameservice.exception.GameApiException;
import io.kk.gameservice.exception.GameNotFoundException;
import io.kk.gameservice.model.ListType;
import io.kk.gameservice.repository.GameListRepository;
import io.kk.gameservice.util.APICallEndpoint;
import io.kk.gameservice.util.APICallParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
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

    public GameDetailsDTO getGame(Long guid) {
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

                List<String> platforms = new ArrayList<>();
                for (JsonNode platformWrapper : rootNode.path("platforms")) {
                    String platformName = platformWrapper.path("platform").path("name").asText();
                    platforms.add(platformName);
                }

                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
                String date = rootNode.path("released").asText();
                Date releaseDate = Objects.equals(date, "null") ? null : formatter.parse(date);

                return GameDetailsDTO.builder()
                        .guid(guid)
                        .name(String.valueOf(rootNode.path("name")))
                        .genres(genres)
                        .platforms(platforms)
                        .backgroundImage(rootNode.path("background_image").asText())
                        .releaseDate(releaseDate)
                        .description(rootNode.path("description").asText())
                        .build();
            }
        } catch (IOException | InterruptedException | ParseException e) {
            throw new RuntimeException(e);
        }

        throw new GameNotFoundException("Game not found");
    }

    public List<GameDTO> getGames(Integer pageNumber, String nameSearch, Long userId, List<String> gameGenres, List<String> gamePlatforms, String dateRange) {
        APICallParams params = new APICallParams();
        params.addParam("page_size", String.valueOf(pageSize));

        if (pageNumber != null && pageNumber > 0) {
            params.addParam("page", String.valueOf(pageNumber));
        }

        if (nameSearch != null && !nameSearch.isEmpty()) {
            params.addParam("search", nameSearch);
        }

        if (gameGenres != null && !gameGenres.isEmpty()) {
            String genresParam = String.join(",", gameGenres.stream()
                    .map(s -> s.toLowerCase().replace(" ", "-"))
                    .toList());
            params.addParam("genres", genresParam);
        }

        if (gamePlatforms != null && !gamePlatforms.isEmpty()) {
            String platformsParam = String.join(",", gamePlatforms.stream()
                    .map(s -> s.toLowerCase().replace(" ", "-"))
                    .toList());
            params.addParam("platforms", platformsParam);
        }

        if (dateRange != null && !dateRange.isEmpty()) {
            params.addParam("dates", dateRange);
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

                List<GameDTO> games = new ArrayList<>();

                for (JsonNode elemNode : rootNode) {
                    GameDTO.GameDTOBuilder game = GameDTO.builder();

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
                    game.backgroundImage(elemNode.get("background_image").asText());

                    List<String> genres = new ArrayList<>();
                    for (JsonNode genreNode : elemNode.path("genres")) {
                        genres.add(genreNode.get("name").asText());
                    }
                    game.genres(genres);

                    List<String> platforms = new ArrayList<>();
                    for (JsonNode platformWrapper : elemNode.path("platforms")) {
                        String platformName = platformWrapper.path("platform").path("name").asText();
                        platforms.add(platformName);
                    }
                    game.platforms(platforms);

                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
                    String date = elemNode.path("released").asText();
                    Date releaseDate = Objects.equals(date, "null") ? null : formatter.parse(date);
                    game.releaseDate(releaseDate);

                    games.add(game.build());
                }

                return games;
            }
        } catch (IOException | InterruptedException | ParseException e) {
            throw new RuntimeException(e);
        }

        throw new GameNotFoundException("Game not found");
    }

    @Cacheable("gameParams")
    public GameParamsDTO getParams() {
        List<ParamDTO> genres = fetchNamesFromEndpoint(APICallEndpoint.GENRES.label);
        List<ParamDTO> platforms = fetchNamesFromEndpoint(APICallEndpoint.PLATFORMS.label);

        genres = genres.stream().sorted(Comparator.comparing(ParamDTO::name)).toList();
        platforms = platforms.stream().sorted(Comparator.comparing(ParamDTO::name)).toList();

        return GameParamsDTO.builder()
                .genres(genres)
                .platforms(platforms)
                .build();
    }

    private List<ParamDTO> fetchNamesFromEndpoint(String endpoint) {
        APICallParams params = new APICallParams();
        URI requestURI = createURI(endpoint, params);

        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(requestURI)
                    .GET()
                    .header("Content-Type", "application/json")
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode rootNode = objectMapper.readTree(response.body());
                JsonNode resultsNode = rootNode.path("results");

                List<ParamDTO> dtos = new ArrayList<>();
                for (JsonNode node : resultsNode) {
                    var dto = ParamDTO.builder()
                            .name(node.get("name").asText())
                            .slug(node.get("slug").asText())
                            .build();
                    dtos.add(dto);
                }
                return dtos;
            }

            log.warn("API returned status {} for endpoint {}", response.statusCode(), endpoint);
            return Collections.emptyList();

        } catch (IOException | InterruptedException e) {
            log.error("Error while fetching data from RAWG API endpoint: {}", endpoint, e);
            throw new GameApiException("Failed to fetch params from API: " + e);
        }
    }
}
