import {useAuthStore} from "@/store/auth";
import {GameListResponse} from "@/types/types";

const API_URL = 'http://localhost:9000/list';

export const gameListService = {
    async getUserLists() {
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

        const data: GameListResponse =  await response.json();
        return data;
    }
}