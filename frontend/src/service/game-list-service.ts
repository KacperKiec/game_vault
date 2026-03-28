import {useAuthStore} from "@/store/auth";
import {GameListRequest, GameListResponse} from "@/types/types";
import {useUiStore} from "@/store/ui";

const API_URL = 'http://localhost:9000/list';

export const gameListService = {
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