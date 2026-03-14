import { createRouter, createWebHistory } from 'vue-router'
import GameGrid from "@/components/games/GameGrid.vue";

const router = createRouter({
    history: createWebHistory(),
    routes: [
        { path: '/games', component: GameGrid }
    ]
})

export default router