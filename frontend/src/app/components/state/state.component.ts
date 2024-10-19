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
  readableState!: string;

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
