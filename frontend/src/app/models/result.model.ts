export interface Result {
    id: number;
    pair: string;
    timeframe: string;
    action: string;
    targetPips: number;
    profit: number;
    startTime: string;
    endTime: string;
    maxDrawdown: number;
    minProgress: number;
    maxProgress: number;
  }
  