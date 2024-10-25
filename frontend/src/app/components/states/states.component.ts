import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NgbPopoverModule } from '@ng-bootstrap/ng-bootstrap';
import { StateComponent } from '../state/state.component';
import { mergeMap, switchMap } from 'rxjs/operators';
import { Pair } from '../../models/pair.model';
import { DataService } from '../../services/data.service';
import { Action } from "../../models/action.model";
import { interval, Subscription } from 'rxjs';

@Component({
  selector: 'states',
  standalone: true,
  imports: [CommonModule, NgbPopoverModule, StateComponent],
  templateUrl: './states.component.html',
  styleUrl: './states.component.css'
})
export class StatesComponent implements OnInit {

  pairs: Pair[] = [];

  constructor(private dataService: DataService) {}

  ngOnInit(): void {
    // query for states 
    this.dataService.getStates().subscribe(states => {
      this.pairs = states;
    });
 }
}