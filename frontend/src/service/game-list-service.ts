import {useAuthStore} from "@/store/auth";
import {GameListRequest, GameListResponse} from "@/types/types";
import {useUiStore} from "@/store/ui";

const API_URL = 'http://localhost:9000/list';

export const gameListService = {
    /**
     * Fetches all game lists associated with the currently authenticated user.
     * This includes categories like wishlist, games to play, and completed games.
     * * @returns {Promise<GameListResponse>} - A promise that resolves to an object containing categorized game lists.
     * @throws {Error} - Throws an error if the request fails or if the user is unauthorized.
     */
    async getUserLists(): Promise<GameListResponse> {
        const uiStore = useUiStore();
        uiStore.setLoading(true);

        try {
            const authStore = useAuthStore();
            const token = authStore.token;

            const response = await fetch(`${API_URL}`, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
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
     * Modifies the user's game lists by adding, moving, or removing a game.
     * The specific action is determined by the listType provided in the DTO.
     * * @param {GameListRequest} dto - Data transfer object containing the game GUID and the target list type.
     * @returns {Promise<boolean>} - Returns true if the operation was successful.
     * @throws {Error} - Throws an error if the modification fails on the server side.
     */
    async modifyLists(dto: GameListRequest): Promise<boolean> {
        const uiStore = useUiStore();
        uiStore.setLoading(true);

        try {
            const authStore = useAuthStore();
            const token = authStore.token;

            const response = await fetch(`${API_URL}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify(dto)
            });

            if (!response.ok) {
                throw new Error(`Error: ${response.status}`);
            }
            return true;
        } finally {
            setTimeout(() => uiStore.setLoading(false), 400);
        }
    }
}