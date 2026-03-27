import { createRouter, createWebHistory } from 'vue-router'
import GameGrid from "@/components/games/GameGrid.vue";

const router = createRouter({
    history: createWebHistory(),
    routes: [
        {
            path: '/',
            alias: '/games',
            name: 'home',
            component: GameGrid,
            props: true
        }
    ]
});

export default router