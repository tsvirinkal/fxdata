import { Component } from '@angular/core';
import { NgbNavModule } from '@ng-bootstrap/ng-bootstrap';
import { StatesComponent } from "../states/states.component";
import { ActionsComponent } from "../actions/actions.component";
import { ResultsComponent } from '../results/results.component';

@Component({
  selector: 'nav',
  standalone: true,
  imports: [NgbNavModule, StatesComponent, ActionsComponent, ResultsComponent],
  templateUrl: './nav.component.html',
  styleUrl: './nav.component.css'
})
export class NavComponent {
  active = 1;
}
