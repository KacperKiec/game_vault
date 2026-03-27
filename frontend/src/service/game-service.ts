import {useAuthStore} from "@/store/auth";
import {Game, GameAPIParams, GameParams} from "@/types/types";

const API_URL = 'http://localhost:9000/api';

export const gameService = {
    async getGames(page: number = 0, gameApiParams: GameAPIParams): Promise<Game[]> {
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
    },

    async getGameParams() {
        const response = await fetch(`${API_URL}/params`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
            }
        });

        if (!response.ok) {
            throw new Error(`Error: ${response.status}`);
        }

        const data: GameParams = await response.json();
        return data;
    }
}