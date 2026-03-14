import { defineStore } from 'pinia';
import { ref, computed } from 'vue';

export const useAuthStore = defineStore('auth', () => {

    const token = ref<string | null>(localStorage.getItem('token'));
    const username = ref<string | null>(localStorage.getItem('username'));
    const role = ref<string>(localStorage.getItem('role') || 'GUEST');

    const isAuthenticated = computed(() => !!token.value);

    function login(newToken: string, newUser: string, newRole: string) {
        token.value = newToken;
        username.value = newUser;
        role.value = newRole;

        localStorage.setItem('token', newToken);
        localStorage.setItem('username', newUser);
        localStorage.setItem('role', newRole);
    }

    function logout() {
        token.value = null;
        username.value = null;
        role.value = 'GUEST';

        localStorage.clear();
    }

    return { token, username, role, isAuthenticated, login, logout };
});