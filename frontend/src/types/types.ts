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
    OWNED = 'OWNED',
    COMPLETED = 'COMPLETED'
}

export interface Game {
    guid: number,
    name: string,
    genres: string[],
    platforms: string[],
    backgroundImage: string,
    releaseDate: Date,
    listType: ListType
}

export interface GameDetails {
    guid: number,
    name: string,
    genres: string[],
    platforms: string[],
    backgroundImage: string,
    releaseDate: Date,
    listType: ListType,
    description: string,
    website: string
}

export interface GameResponse {
    games: Game[],
    totalPages: number
}

export interface GameListResponse {
    wishlist: Game[],
    ownedGames: Game[],
    completedGames: Game[]
}

export interface Param {
    id: number,
    name: string,
    slug: string
}

export interface GameParams {
    genres: Param[],
    platforms: Param[]
}

export interface GameAPIParams {
    gameName: string,
    gameParams: GameParams,
    gameDates: string,
}

export interface GameListRequest {
    guid: number,
    listType: ListType
}

export interface Review {
    id: number,
    username :string,
    content: string,
    rating: number,
    date: Date,
}

export interface ReviewRequest {
    guid: number,
    gameName: string,
    content: string,
    rating: number
}

export enum NotificationType {
    NEW_REVIEW = "NEW_REVIEW"
}

export interface AppNotification {
    id: number;
    type: NotificationType;
    title: string;
    content: string;
    metadata: Record<string, any>;
    createdAt: Date;
    read: boolean;
}