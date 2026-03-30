import {useAuthStore} from "@/store/auth";
import {Review, ReviewRequest} from "@/types/types";
import {useUiStore} from "@/store/ui";

const API_URL = 'http://localhost:9000/review';

export const reviewService = {
    /**
     * Submits a new review for a game.
     * Requires an active user session and a valid JWT token.
     * * @param {ReviewRequest} dto - Data transfer object containing the game ID, rating, and review content.
     * @returns {Promise<Review>} - A promise that resolves to the newly created review object.
     * @throws {Error} - Throws an error if the request fails or if the user is unauthorized.
     */
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

    /**
     * Permanently deletes a review from the system by its ID.
     * This operation requires administrative or owner privileges verified via JWT.
     * * @param {number} reviewId - The unique identifier of the review to be removed.
     * @returns {Promise<boolean>} - Returns true if the deletion was successful.
     * @throws {Error} - Throws an error if the server responds with a failure status.
     */
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

    /**
     * Fetches all reviews associated with a specific game.
     * This is a public endpoint that does not require authentication.
     * * @param {number} guid - The unique global identifier (GUID) of the game.
     * @returns {Promise<Review[]>} - A promise that resolves to an array of review objects for the game.
     * @throws {Error} - Throws an error if the fetch operation fails.
     */
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