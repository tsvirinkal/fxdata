export class Action {
    id: number;
    action: string;
    target: number;
    time: string;

    constructor(id: number, action: string, time: string, target: number) {
      this.id=id;
      this.action=action;
      this.time=time
      this.target=target;
    }
  }
  