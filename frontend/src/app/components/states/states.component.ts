import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { StateItemComponent } from '../state-item/state-item.component';
import { mergeMap } from 'rxjs/operators';
import { Pair } from '../../models/pair.model';
import { DataService } from '../../services/data.service';
import { Action } from "../../models/action.model";

@Component({
  selector: 'state',
  standalone: true,
  imports: [CommonModule, StateItemComponent],
  templateUrl: './states.component.html',
  styleUrl: './states.component.css'
})
export class StatesComponent implements OnInit {

  items: Pair[] = [];
  internal: Pair[] = [];

  constructor(private dataService: DataService) {}

  ngOnInit(): void {
    // query for states 
    this.dataService.getStates()
    .pipe(
      mergeMap(states => {
        this.internal = states;
        // query for action updates
        return this.dataService.getData();
      })
    )
    .subscribe(data => {
      for (const day of data.reverse()) {
        for (const rec of day.records.reverse()) {
          const pair = this.internal.find(item => item.name === rec.pair);
          
          if (!pair) continue;
          const state = pair.states.find(state => state.timeframe === rec.timeframe);

          if (!state || !rec.action) continue;
          if (!state.action) {
            state.action=new Action(0, rec.action, rec.time, 0, 0);
          }
          
          state.action!.action = rec.action;
          state.action!.time = rec.time;
          state.action!.progress = Math.floor(Math.random()*70);
          state.action!.target = Math.floor(Math.random()*300);
        }
      }
      this.items = this.internal;
    });
  }
}