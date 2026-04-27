import {useAuthStore} from "@/store/auth";
import {DashboardDocument} from "@/types/types";
import {useUiStore} from "@/store/ui";

const API_URL = `${import.meta.env.VITE_API_URL}/dashboard`;

export const dashboardService = {

    async getDashboard(): Promise<DashboardDocument> {
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

            const response = await fetch(`${API_URL}`, {
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