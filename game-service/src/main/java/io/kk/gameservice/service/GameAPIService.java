package io.kk.gameservice.service;

import io.kk.gameservice.dto.*;
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
import java.util.stream.Collectors;

/**
 * Service responsible for interacting with the external RAWG.io API.
 * It provides functionality to fetch game details, list of games with filters,
 * and metadata such as genres and platforms.
 */
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

    /**
     * Constructs a full URI for the RAWG API including the endpoint and query parameters.
     *
     * @param endpoint The specific API endpoint (e.g., "/games").
     * @param params   Custom parameters to be appended to the query string.
     * @return A {@link URI} object representing the full request path.
     */
    private URI createURI(String endpoint, APICallParams params) {
        String uri = apiURI + endpoint + "?key=" + apiKey + params;
        return URI.create(uri);
    }

    /**
     * Fetches detailed information about a single game by its unique identifier.
     * Maps external API data to a {@link GameDetailsDTO} and checks the user's local list status.
     *
     * @param guid   The unique ID of the game in the external API.
     * @param userId The ID of the local user to check if the game exists in their collection.
     * @return A {@link GameDetailsDTO} containing comprehensive game information.
     * @throws GameNotFoundException if the API returns a non-200 status or parsing fails.
     */
    public GameDetailsDTO getGame(Long guid, Long userId) {
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

                ListType type  = ListType.NONE;
                if (userId != null) {
                    var gameList = gameListRepository.findByUserIdAndGameId(userId, guid);
                    if  (gameList.isEmpty())
                        type = ListType.NONE;
                    else
                        type = gameList.get().getListType();
                }

                return GameDetailsDTO.builder()
                        .guid(guid)
                        .name(rootNode.path("name").asText())
                        .genres(genres)
                        .platforms(platforms)
                        .backgroundImage(rootNode.path("background_image").asText())
                        .releaseDate(releaseDate)
                        .description(rootNode.path("description").asText())
                        .listType(type)
                        .website(rootNode.path("website").asText())
                        .build();
            }
        } catch (IOException | InterruptedException | ParseException e) {
            throw new RuntimeException("Failed to fetch game details", e);
        }

        throw new GameNotFoundException("Game not found");
    }

    /**
     * Retrieves a paginated list of games based on various filter criteria.
     *
     * @param pageNumber    The current page to fetch.
     * @param nameSearch    Partial name string to search for.
     * @param userId        The ID of the local user to check list statuses for each game.
     * @param gameGenres    List of genre IDs to filter by.
     * @param gamePlatforms List of platform IDs to filter by.
     * @param dateRange     Release date range string (e.g., "2020-01-01,2020-12-31").
     * @return A {@link GameResponseDTO} containing the list of games and total page count.
     * @throws GameNotFoundException if the API returns a non-200 status or parsing fails.
     */
    public GameResponseDTO getGames(Integer pageNumber, String nameSearch, Long userId, List<Long> gameGenres, List<Long> gamePlatforms, String dateRange) {
        APICallParams params = new APICallParams();
        params.addParam("page_size", String.valueOf(pageSize));

        if (pageNumber != null && pageNumber > 0) {
            params.addParam("page", String.valueOf(pageNumber));
        }

        if (nameSearch != null && !nameSearch.isEmpty()) {
            params.addParam("search", nameSearch);
        }

        if (gameGenres != null && !gameGenres.isEmpty()) {
            String genresParam = gameGenres.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));
            params.addParam("genres", genresParam);
        }

        if (gamePlatforms != null && !gamePlatforms.isEmpty()) {
            String platformsParam = gamePlatforms.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));
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

                JsonNode root = objectMapper.readTree(response.body());
                int count = root.path("count").asInt();
                int totalPages = (int) Math.ceil((double) count / pageSize);

                JsonNode results = root.path("results");
                List<GameDTO> games = new ArrayList<>();

                for (JsonNode elemNode : results) {
                    GameDTO.GameDTOBuilder game = GameDTO.builder();
                    Long guid = elemNode.path("id").asLong();
                    game.guid(guid);

                    if (userId != null) {
                        var gameList = gameListRepository.findByUserIdAndGameId(userId, guid);
                        game.listType(gameList.isEmpty() ? ListType.NONE : gameList.get().getListType());
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
                        platforms.add(platformWrapper.path("platform").path("name").asText());
                    }
                    game.platforms(platforms);

                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
                    String date = elemNode.path("released").asText();
                    Date releaseDate = Objects.equals(date, "null") ? null : formatter.parse(date);
                    game.releaseDate(releaseDate);

                    games.add(game.build());
                }

                return GameResponseDTO.builder()
                        .games(games)
                        .totalPages(totalPages)
                        .build();

            }
        } catch (IOException | InterruptedException | ParseException e) {
            throw new RuntimeException("Failed to fetch games list", e);
        }

        throw new GameNotFoundException("Game not found");
    }

    /**
     * Fetches metadata for genres and platforms and caches the result.
     * Sorting is performed alphabetically by name.
     *
     * @return A {@link GameParamsDTO} containing sorted lists of genres and platforms.
     */
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

    /**
     * Generic helper method to fetch metadata from a specific RAWG endpoint.
     *
     * @param endpoint The API endpoint to target (e.g., genres, platforms).
     * @return A list of {@link ParamDTO} objects.
     * @throws GameApiException if an error occurs during API communication.
     */
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
                            .id(node.get("id").asLong())
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
            throw new GameApiException("Failed to fetch params from API: " + e.getMessage());
        }
    }
}