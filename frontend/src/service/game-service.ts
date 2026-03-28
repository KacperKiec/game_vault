import {useAuthStore} from "@/store/auth";
import {GameAPIParams, GameDetails, GameParams, GameResponse} from "@/types/types";
import {useUiStore} from "@/store/ui";

const API_URL = 'http://localhost:9000/api';

export const gameService = {
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
                queryParams.append('game_genres', genres.join(','));
            }

            const platforms = gameApiParams.gameParams.platforms;
            if (platforms && platforms.length > 0) {
                queryParams.append('game_platforms', platforms.join(','));
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