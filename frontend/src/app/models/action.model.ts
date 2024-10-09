export class Action {
    id: number;
    action: string;
    targetPips: number;
    time: string;
    entryPrice: number;
    startPrice: number;
    targetPrice: number;

    constructor(id: number, action: string, targetPips: number, time: string, entryPrice: number, startPrice: number, targetPrice: number) {
      this.id=id;
      this.action=action;
      this.time=time
      this.targetPips=targetPips;
      this.entryPrice=entryPrice;
      this.startPrice=startPrice;
      this.targetPrice=targetPrice;
    }
  }
  