import { Action } from "./action.model";

export interface State {
    action?: Action;
    actions: number[];
    state: string;
    time: string;
    timeframe: string;
    progress: number;
  }
  