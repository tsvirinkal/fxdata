import { Component, OnInit, NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NgbPopoverModule } from '@ng-bootstrap/ng-bootstrap';
import { StateComponent } from '../state/state.component';
import { Pair } from '../../models/pair.model';
import { DataService } from '../../services/data.service';
import { FormsModule } from '@angular/forms'; 

@Component({
  selector: 'states',
  standalone: true,
  imports: [CommonModule, NgbPopoverModule, FormsModule, StateComponent],
  templateUrl: './states.component.html',
  styleUrl: './states.component.css'
})
export class StatesComponent implements OnInit {

  pairs: Pair[] = [];
  editedPair?: Pair;
  selectedTf?: string;
  selectedTfIndex = 0;
  constructor(private dataService: DataService) {}

  ngOnInit(): void {
    // query for states 
    this.dataService.getStates().subscribe(states => {
      this.pairs = states;
    });
  }

  setEditedPair(pair: string) {
    this.editedPair = this.pairs.find(p => p.name === pair)
    for(var i=0; i<3; i++) {
      var st = this.editedPair?.states[i];
      if (st && st.active) {
        this.selectedTf = st.timeframe;
        this.selectedTfIndex = i;
      }
    }
    console.log("loaded: "+this.selectedTf+", index: "+ this.selectedTfIndex+" for "+this.editedPair?.name);
  }

  onSave() {
    console.log('saved: '+ this.selectedTf+" for "+this.editedPair?.name);
    if (this.editedPair && this.selectedTf) {
      this.editedPair.activeTf = this.selectedTf;
      this.dataService.setActiveState(this.editedPair.name, this.selectedTf);
      this.editedPair?.states.forEach(s => s.active=false);
      const state = this.editedPair?.states.find(s => s.timeframe===this.selectedTf);
      if (state) {
        state.active = true;
      }
    }
    this.editedPair = undefined;
    this.selectedTf = undefined;
  }
}