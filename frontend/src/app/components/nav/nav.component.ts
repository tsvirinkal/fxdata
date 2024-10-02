import { Component } from '@angular/core';
import { NgbNavModule } from '@ng-bootstrap/ng-bootstrap';
import { StateComponent } from "../state/state.component";
import { ListGroupComponent } from "../list-group/list-group.component";

@Component({
  selector: 'nav',
  standalone: true,
  imports: [NgbNavModule, StateComponent, ListGroupComponent],
  templateUrl: './nav.component.html',
  styleUrl: './nav.component.css'
})
export class NavComponent {
  active = 1;
}
