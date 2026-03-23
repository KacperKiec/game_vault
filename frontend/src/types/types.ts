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

export enum ListType {
    NONE = "NONE",
    WISHLIST = 'WISHLIST',
    TODO = 'TODO',
    COMPLETED = 'COMPLETED'
}

export interface GameResponse {
    guid: number,
    name: string,
    category: string[],
    imageURL: string,
    releaseDate: Date,
    description: string
}

export interface GameListResponse {
    wishlist: GameResponse[],
    gamesToPlay: GameResponse[],
    completedGames: GameResponse[]
}