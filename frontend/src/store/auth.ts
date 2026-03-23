import {defineStore} from 'pinia';
import {computed, ref} from 'vue';

export const useAuthStore = defineStore('auth', () => {

    const userId = ref<string | null>(localStorage.getItem('userId'));
    const token = ref<string | null>(localStorage.getItem('token'));
    const username = ref<string | null>(localStorage.getItem('username'));
    const role = ref<string>(localStorage.getItem('role') || 'GUEST');

    const isAuthenticated = computed(() => !!token.value);

    function login(newToken: string, newUser: string, newRole: string, newId: number) {
        token.value = newToken;
        username.value = newUser;
        role.value = newRole;
        userId.value = newId.toString();

        localStorage.setItem('token', newToken);
        localStorage.setItem('username', newUser);
        localStorage.setItem('role', newRole);
        localStorage.setItem('userId', newId.toString());
    }

    function logout() {
        token.value = null;
        username.value = null;
        role.value = 'GUEST';
        userId.value = null;

        localStorage.clear();
    }

    return { userId, token, username, role, isAuthenticated, login, logout };
});
