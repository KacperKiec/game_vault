import {defineStore} from 'pinia';
import {computed, ref} from 'vue';

export const useAuthStore = defineStore('auth', () => {

    const SESSION_DURATION = 60 * 60 * 1000;

    const userId = ref<string | null>(localStorage.getItem('userId'));
    const token = ref<string | null>(localStorage.getItem('token'));
    const username = ref<string | null>(localStorage.getItem('username'));
    const role = ref<string>(localStorage.getItem('role') || 'GUEST');

    let logoutTimer: ReturnType<typeof setTimeout> | null = null;

    const isAuthenticated = computed(() => !!token.value);

    function checkSessionHealth() {
        const loginTimestamp = localStorage.getItem('loginTimestamp');
        if (loginTimestamp) {
            const now = Date.now();
            const elapsed = now - parseInt(loginTimestamp);

            if (elapsed >= SESSION_DURATION) {
                logout();
            } else {
                setLogoutTimer(SESSION_DURATION - elapsed);
            }
        }
    }

    function setLogoutTimer(duration: number) {
        if (logoutTimer) clearTimeout(logoutTimer);
        logoutTimer = setTimeout(() => {
            logout();
        }, duration);
    }

    function login(newToken: string, newUser: string, newRole: string, newId: number) {
        token.value = newToken;
        username.value = newUser;
        role.value = newRole;
        userId.value = newId.toString();

        localStorage.setItem('token', newToken);
        localStorage.setItem('username', newUser);
        localStorage.setItem('role', newRole);
        localStorage.setItem('userId', newId.toString());

        if (logoutTimer) clearTimeout(logoutTimer);
        setLogoutTimer(SESSION_DURATION);
    }

    function logout() {
        token.value = null;
        username.value = null;
        role.value = 'GUEST';
        userId.value = null;

        localStorage.clear();
    }

    checkSessionHealth();

    return { userId, token, username, role, isAuthenticated, login, logout };
});
