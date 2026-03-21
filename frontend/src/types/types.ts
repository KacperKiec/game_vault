export interface AuthRequest {
    username: string;
    password: string;
}

export interface AuthResponse {
    token: string;
    userId: number,
    username: string;
    role: string;
}

export interface UserRegister {
    email: string;
    password: string;
    username: string;
}