import { createRouter, createWebHistory } from 'vue-router'
import GameGrid from "@/components/games/GameGrid.vue";
import GameDetail from "@/components/games/GameDetail.vue";
import Dashboard from "@/components/Dashboard.vue";

const router = createRouter({
    history: createWebHistory(),
    routes: [
        {
            path: '/',
            alias: '/games',
            name: 'home',
            component: GameGrid,
            props: true
        },
        {
            path: '/details/:id',
            name: 'game-details',
            component: GameDetail,
            props: true
        },
        {
            path: '/dashboard',
            name: 'dashboard',
            component: Dashboard,
            props: false
        }
    ]
});

export default router