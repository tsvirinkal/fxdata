import { Component, Input } from '@angular/core';
import { NgClass, NgIf } from '@angular/common'; 
import { State } from '../../models/state.model';
import { NgbProgressbarModule } from '@ng-bootstrap/ng-bootstrap';
import { Action } from 'rxjs/internal/scheduler/Action';

@Component({
  selector: 'state-item',
  standalone: true,
  imports: [NgbProgressbarModule, NgClass, NgIf],
  templateUrl: './state-item.component.html',
  styleUrl: './state-item.component.css'
})
export class StateItemComponent {
  @Input() item!: State;

  getStateClass(): string {
    if(!this.item) return 'range';
    return this.item.state.toLowerCase();
  }

  hasAction(): boolean {
    return this.item.action instanceof Action;
  }
}
