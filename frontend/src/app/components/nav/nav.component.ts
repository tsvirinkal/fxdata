import { Component } from '@angular/core';
import { NgbNavModule } from '@ng-bootstrap/ng-bootstrap';
import { StatesComponent } from "../states/states.component";
import { ListGroupComponent } from "../list-group/list-group.component";

@Component({
  selector: 'nav',
  standalone: true,
  imports: [NgbNavModule, StatesComponent, ListGroupComponent],
  templateUrl: './nav.component.html',
  styleUrl: './nav.component.css'
})
export class NavComponent {
  active = 1;
}
