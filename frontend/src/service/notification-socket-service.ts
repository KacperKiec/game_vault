import SockJS from 'sockjs-client';
import {CompatClient, Stomp} from '@stomp/stompjs';
import { ref } from 'vue';
import { useAuthStore } from "@/store/auth";

const API_URL = 'http://localhost:9000';

const stompClient = ref<CompatClient | null>(null);
const isConnected = ref<boolean>(false);
const latestNotification = ref<any>(null);

export const notificationSocketService = {
    latestNotification,
    isConnected,

    connect() {
        if (isConnected.value) return;

        const authStore = useAuthStore();
        const userId = authStore.userId;
        const token = authStore.token;

        const socket = new SockJS(`${API_URL}/ws/notifications`, null, {
            transports: ['websocket']
        });
        stompClient.value = Stomp.over(socket);

        stompClient.value.debug = () => {};

        const headers = {
            Authorization: `Bearer ${token}`
        };

        stompClient.value.connect(headers, () => {
            isConnected.value = true;

            if (userId && stompClient.value) {
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

    disconnect() {
        if (stompClient.value && isConnected.value) {
            stompClient.value.disconnect(() => {
                isConnected.value = false;
                console.log("Disconnected from WebSocket");
            });
        }
    }
};