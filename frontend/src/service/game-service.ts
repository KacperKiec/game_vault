import {useAuthStore} from "@/store/auth";
import {GameAPIParams, GameDetails, GameParams, GameResponse} from "@/types/types";
import {useUiStore} from "@/store/ui";

const API_URL = 'http://localhost:9000/api';

export const gameService = {
    /**
     * Fetches a paginated list of games from the API based on search criteria and filters.
     * Construct query parameters for name, release dates, genres, and platforms.
     * * @param {number} [page=1] - The page number to retrieve.
     * @param {number} page - Page number in query
     * @param {GameAPIParams} gameApiParams - Filtering parameters including search name, date range, and selected genres/platforms.
     * @returns {Promise<GameResponse>} - A promise that resolves to an object containing the list of games and pagination metadata.
     * @throws {Error} - Throws an error if the server responds with a non-OK status.
     */
    async getGames(page: number = 1, gameApiParams: GameAPIParams): Promise<GameResponse> {
        const uiStore = useUiStore();
        uiStore.setLoading(true);

        try {
            const authStore = useAuthStore();
            const token = authStore.token;

            const queryParams = new URLSearchParams();

            queryParams.append('page_number', page.toString());

            const gameName = gameApiParams.gameName;
            if (gameName) {
                queryParams.append('name_search', gameName);
            }

            const dates = gameApiParams.gameDates;
            if (dates) {
                queryParams.append('dates', dates);
            }

            const genres = gameApiParams.gameParams.genres;
            if (genres && genres.length > 0) {
                const genreIds = genres.map(g => g.id).join(',');
                queryParams.append('game_genres', genreIds);
            }

            const platforms = gameApiParams.gameParams.platforms;
            if (platforms && platforms.length > 0) {
                const platformIds = platforms.map(p => p.id).join(',');
                queryParams.append('game_platforms', platformIds);
            }

            const headers: Record<string, string> = {
                'Content-Type': 'application/json'
            };

            if (token) {
                headers['Authorization'] = `Bearer ${token}`;
            }

            const response = await fetch(`${API_URL}/games?${queryParams}`, {
                method: 'GET',
                headers: headers
            });

            if (!response.ok) {
                throw new Error(`Error: ${response.status}`);
            }

            return await response.json();
        } finally {
            setTimeout(() => uiStore.setLoading(false), 400);
        }
    },

    /**
     * Retrieves the available filtering parameters (genres and platforms) from the backend.
     * This is typically used to populate the sidebar or filter selection UI.
     * * @returns {Promise<GameParams>} - A promise that resolves to an object containing arrays of available genres and platforms.
     * @throws {Error} - Throws an error if the parameters could not be fetched.
     */
    async getGameParams(): Promise<GameParams> {
        const uiStore = useUiStore();
        uiStore.setLoading(true);

        try {
            const response = await fetch(`${API_URL}/params`, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                }
            });

            if (!response.ok) {
                throw new Error(`Error: ${response.status}`);
            }

            return await response.json();
        } finally {
            setTimeout(() => uiStore.setLoading(false), 400);
        }
    },

    /**
     * Fetches detailed information for a single game identified by its GUID.
     * * @param {number} guid - The unique identifier of the game.
     * @returns {Promise<GameDetails>} - A promise that resolves to the full details of the specific game.
     * @throws {Error} - Throws an error if the game is not found or the request fails.
     */
    async getGameById(guid: number): Promise<GameDetails> {
        const uiStore = useUiStore();
        uiStore.setLoading(true);

        try {
            const authStore = useAuthStore();
            const token = authStore.token;

            const headers: Record<string, string> = {
                'Content-Type': 'application/json'
            };

            if (token) {
                headers['Authorization'] = `Bearer ${token}`;
            }

            const response = await fetch(`${API_URL}/game/${guid}`, {
                method: 'GET',
                headers: headers
            });

            if (!response.ok) {
                throw new Error(`Error: ${response.status}`);
            }

            return await response.json();
        } finally {
            setTimeout(() => uiStore.setLoading(false), 400);
        }
    }
}