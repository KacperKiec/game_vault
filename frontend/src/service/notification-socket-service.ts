import SockJS from 'sockjs-client';
import {CompatClient, Stomp} from '@stomp/stompjs';
import { ref } from 'vue';
import { useAuthStore } from "@/store/auth";

const API_URL = `${import.meta.env.VITE_API_URL}`;
const stompClient = ref<CompatClient | null>(null);
const isConnected = ref<boolean>(false);
const latestNotification = ref<any>(null);

/**
 * Service responsible for managing WebSocket connections via STOMP protocol.
 * It enables real-time notification updates for the authenticated user.
 */
export const notificationSocketService = {
    /**
     * Reactive reference to the most recently received notification object.
     * @type {Ref<any>}
     */
    latestNotification,

    /**
     * Reactive boolean flag indicating the current connection status.
     * @type {Ref<boolean>}
     */
    isConnected,

    /**
     * Establishes a connection to the WebSocket server using SockJS and STOMP.
     * It uses the user's JWT token for authentication and subscribes to a private
     * topic based on the user's ID to receive personalized notifications.
     *
     * @returns {void}
     * @throws {Error} Logs connection errors to the console if the handshake fails.
     */
    connect() {
        if (isConnected.value) return;

        const authStore = useAuthStore();
        const userId = authStore.userId;
        const token = authStore.token;

        const socket = new SockJS(`${API_URL}/ws/notifications`, null, {
            transports: ['websocket']
        });
        stompClient.value = Stomp.over(socket);

        // Disable debug logging for a cleaner console
        stompClient.value.debug = () => {};

        const headers = {
            Authorization: `Bearer ${token}`
        };

        stompClient.value.connect(headers, () => {
            isConnected.value = true;

            if (userId && stompClient.value) {
                // Subscribe to a user-specific topic to receive real-time updates
                stompClient.value.subscribe(`/topic/notification/${userId}`, (message) => {
                    if (message.body) {
                        latestNotification.value = JSON.parse(message.body);
                    }
                });
            }
        }, (error: any) => {
            console.error('Socket connection error:', error);
            isConnected.value = false;
        });
    },

    /**
     * Gracefully closes the active WebSocket connection.
     * Resets the connection status flag upon successful disconnection.
     *
     * @returns {void}
     */
    disconnect() {
        if (stompClient.value && isConnected.value) {
            stompClient.value.disconnect(() => {
                isConnected.value = false;
                console.log("Disconnected from WebSocket");
            });
        }
    }
};