import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
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
  constructor(private dataService: DataService, private cdr: ChangeDetectorRef) {}

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
      this.dataService.setActiveState(this.editedPair.name, this.selectedTf).subscribe(
        () => {
          this.ngOnInit();
          this.cdr.detectChanges();
        },
        (error) => {
          console.error('POST error:', error);
        })
    }
    this.editedPair = undefined;
    this.selectedTf = undefined;
  }
  
  getActions(pair: Pair) {
    var index = pair.states.findIndex(s => s.active);
    if (index>=0) {
      const state = pair.states[index];
      return state.actions;
    }
    return []
  }
}