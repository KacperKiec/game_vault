import {AuthRequest, AuthResponse, UserRegister} from "@/types/types";
import {useAuthStore} from "@/store/auth";

const API_URL = 'http://localhost:9000/auth';

export const authService = {
    async login(dto: AuthRequest) {
        const response = await fetch(`${API_URL}/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(dto),
        });

        if (response.status === 401) {
            const errorData = await response.json();
            const message = errorData.user || errorData.message || "Logging in failed";
            throw new Error(message);
        }

        if (!response.ok) {
            throw new Error('Error while logging in');
        }

        const data: AuthResponse = await response.json();

        const authStore = useAuthStore();

        authStore.login(data.token, data.username, data.role, data.userId);

        return true;
    },

    async register(dto: UserRegister) {
        const response = await fetch(`${API_URL}/register`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(dto),
        });

        if (response.status === 400) {
            const errorData = await response.json();
            const message = errorData.email || errorData.message || "Validation failed";
            throw new Error(message);
        }

        if (!response.ok) {
            throw new Error('Error while registering');
        }

        const loginRequest: AuthRequest = {
            username: dto.username,
            password: dto.password
        };

        return this.login(loginRequest);
    }
}