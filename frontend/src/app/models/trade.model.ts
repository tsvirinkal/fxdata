import { Record } from './record.model';

export interface Trade {
    id: number;
    pair: string;
    command: string;
    action: string;
    price: number;
    profit: number;
    openedTime: string;
    error: string;
  }
  