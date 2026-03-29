import {useAuthStore} from "@/store/auth";
import {AppNotification} from "@/types/types";

const API_URL = 'http://localhost:9000/notification';

export const notificationService = {
    async getNotifications(): Promise<AppNotification[]> {
        const authStore = useAuthStore();
        const token = authStore.token;
        const userId = authStore.userId;

        const headers: Record<string, string> = {
            'Content-Type': 'application/json'
        };

        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }

        const response = await fetch(`${API_URL}/unread/${userId}`, {
            method: 'GET',
            headers: headers
        });

        if (!response.ok) {
            throw new Error(`Error: ${response.status}`);
        }

        return await response.json();
    },

    async markNotificationAsRead(notificationId: number): Promise<boolean> {
        const authStore = useAuthStore();
        const token = authStore.token;

        const headers: Record<string, string> = {
            'Content-Type': 'application/json'
        };

        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }

        const response = await fetch(`${API_URL}/${notificationId}`, {
            method: 'PATCH',
            headers: headers
        });

        if (!response.ok) {
            throw new Error(`Error: ${response.status}`);
        }

        return true;
    }
}