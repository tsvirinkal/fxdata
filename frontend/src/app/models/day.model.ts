import { Record } from '../models/record.model';

export interface Day {
    id: number;
    date: string;
    records: Record[];
  }
  