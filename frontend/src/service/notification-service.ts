import {useAuthStore} from "@/store/auth";
import {AppNotification} from "@/types/types";

const API_URL = 'http://localhost:9000/notification';

export const notificationService = {
    /**
     * Retrieves all unread notifications for the currently authenticated user.
     * Uses the userId from AuthStore to target the specific user's notification feed.
     * * @returns {Promise<AppNotification[]>} - A promise that resolves to an array of unread notification objects.
     * @throws {Error} - Throws an error if the request fails or if the user is unauthorized.
     */
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

    /**
     * Marks a specific notification as read by its unique identifier.
     * This operation typically triggers a status update on the backend to hide the notification from the unread list.
     * * @param {number} notificationId - The unique ID of the notification to be updated.
     * @returns {Promise<boolean>} - Returns true if the notification was successfully marked as read.
     * @throws {Error} - Throws an error if the update operation fails on the server.
     */
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