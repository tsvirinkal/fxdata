import { Component } from '@angular/core';
import { Pair } from '../../models/pair.model';
import { DataService } from '../../services/data.service';
import { CommonModule } from '@angular/common';
import { StateItemComponent } from '../state-item/state-item.component';

@Component({
  selector: 'state',
  standalone: true,
  imports: [CommonModule, StateItemComponent],
  templateUrl: './state.component.html',
  styleUrl: './state.component.css'
})
export class StateComponent {

  items: Pair[] = [ 
    { name: "EURUSD", 
      price: 1.2345,
      states: [ { id: 1, state: "Bullish", time: "12:34", timeframe: "3", target: 50, action: "Buy", progress: 90 },
                { id: 2, state: "Bearish", time: "12:34", timeframe: "4", target: 0, action: "", progress: 0 },
                { id: 3, state: "Range", time: "12:34", timeframe: "5", target: 319, action: "Buy", progress: 27 },
      ]
    },
    { name: "AUDNZD", 
      price: 1.2345,
      states: [ { id: 1, state: "Range", time: "12:34", timeframe: "3", target: 40, action: "Sell", progress: 54 },
                { id: 2, state: "Bullish", time: "12:34", timeframe: "4", target: 0, action: "", progress: 0 },
                { id: 3, state: "Bearish", time: "12:34", timeframe: "5", target: 0, action: "", progress: 0 },
      ]
    },
    { name: "CHFJPY", 
      price: 1.2345,
      states: [ { id: 1, state: "Bullish", time: "12:34", timeframe: "3", target: 0, action: "", progress: 0 },
                { id: 2, state: "Range", time: "12:34", timeframe: "4", target: 153, action: "Sell", progress: 71 },
                { id: 3, state: "Bullish", time: "12:34", timeframe: "5", target: 451, action: "Buy", progress: 6 },
      ]
    },
  ];

  constructor(private dataService: DataService) {}

  ngOnInit(): void {
    // this.dataService.getStates().subscribe((data: Pair[]) => {
    //   this.items = data;
    // });
  }
}