import { Action } from "./action.model";

export interface State {
    action?: Action;
    state: string;
    time: string;
    timeframe: string;
  }
  