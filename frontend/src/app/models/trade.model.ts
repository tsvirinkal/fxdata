import { Record } from './record.model';

export interface Trade {
    id: number;
    pair: string;
    command: string;
    action: Record;
    createdTime: string;
    openedTime: string;
    closedTime: string;
  }
  