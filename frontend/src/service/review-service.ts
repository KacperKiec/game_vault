import {useAuthStore} from "@/store/auth";
import {Review, ReviewRequest} from "@/types/types";
import {useUiStore} from "@/store/ui";

const API_URL = 'http://localhost:9000/review';

export const reviewService = {
    async addReview(dto: ReviewRequest): Promise<Review> {
        const uiStore = useUiStore();
        uiStore.setLoading(true);

        try {
            const authStore = useAuthStore();
            const token = authStore.token;

            const response = await fetch(`${API_URL}/add`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify(dto)
            });

            if (!response.ok) {
                throw new Error(`Error: ${response.status}`);
            }

            return response.json();
        } finally {
            setTimeout(() => uiStore.setLoading(false), 400);
        }
    },

    async deleteReview(reviewId: number): Promise<boolean> {
        const uiStore = useUiStore();
        uiStore.setLoading(true);

        try {
            const authStore = useAuthStore();
            const token = authStore.token;

            const response = await fetch(`${API_URL}/delete/${reviewId}`, {
                method: 'DELETE',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                }
            });

            if (!response.ok) {
                throw new Error(`Error: ${response.status}`);
            }

            return true;
        } finally {
            setTimeout(() => uiStore.setLoading(false), 400);
        }
    },

    async getReviews(guid: number): Promise<Review[]> {
        const uiStore = useUiStore();
        uiStore.setLoading(true);

        try {
            const response = await fetch(`${API_URL}/game/${guid}`, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json'
                }
            });

            if (!response.ok) {
                throw new Error(`Error: ${response.status}`);
            }

            return await response.json();
        } finally {
            setTimeout(() => uiStore.setLoading(false), 400);
        }
    },
}