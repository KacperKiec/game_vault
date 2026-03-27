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
}

export interface GameListResponse {
    wishlist: Game[],
    gamesToPlay: Game[],
    completedGames: Game[]
}

export interface Param {
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