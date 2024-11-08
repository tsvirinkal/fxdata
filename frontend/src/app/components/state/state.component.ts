import { Component, Input } from '@angular/core';
import { NgClass, NgIf } from '@angular/common'; 
import { State } from '../../models/state.model';
import { NgbProgressbarModule } from '@ng-bootstrap/ng-bootstrap';
import { Action } from "../../models/action.model";
import { NgbPopoverModule } from '@ng-bootstrap/ng-bootstrap';


@Component({
  selector: 'state',
  standalone: true,
  imports: [NgbProgressbarModule, NgbPopoverModule, NgClass, NgIf],
  templateUrl: './state.component.html',
  styleUrl: './state.component.css'
})
export class StateComponent {
  @Input() item!: State;
  today = 'today';

  constructor() {
    const date = new Date(); 
    const day = String(date.getDate()).padStart(2, '0');
    const month = String(date.getMonth() + 1).padStart(2, '0'); 
    const year = date.getFullYear();
    this.today = `${day}.${month}.${year}`;
  }

  isToday(): boolean {
    return this.item.action!=null && this.item.action.time.includes(this.today);
  }

  getStateClass(): string {
    if (!this.item) return '???';
    return this.item.state.toLowerCase();
  }

  getReadableState(): string {
    if (!this.item) return '???';
    if (this.item.state==='Range') return 'ranging';
    return this.item.state.toLowerCase();
  }

  hasAction(): boolean {
    return this.item.action!=null;
  }
}
