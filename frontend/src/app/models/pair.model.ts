import { Action } from "./action.model";
import { State } from "./state.model";

export interface Pair {
    name: string;
    states: [State, State, State];
    price: number;
    point: number;
    updated: string;
}