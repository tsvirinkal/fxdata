export class Action {
    id: number;
    action: string;
    progress: number;
    target: number;
    time: string;

    constructor(id: number, action: string, time: string, progress: number, target: number) {
      this.id=id;
      this.action=action;
      this.time=time
      this.progress=progress;
      this.target=target;
    }
  }
  