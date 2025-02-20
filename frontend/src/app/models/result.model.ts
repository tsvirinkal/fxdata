export interface Result {
    id: number;
    pair: string;
    timeframe: string;
    action: string;
    state: string;
    entryPrice: number;
    exitPrice: number;
    targetPips: number;
    profit: number;
    maxDrawdown: number;
    minProgress: number;
    maxProgress: number;
    startTime: string;
    endTime: string;
    duration: string;
  }
  