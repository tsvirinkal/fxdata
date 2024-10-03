import { Component, OnInit } from '@angular/core';
import { Pair } from '../../models/pair.model';
import { DataService } from '../../services/data.service';
import { CommonModule } from '@angular/common';
import { StateItemComponent } from '../state-item/state-item.component';

@Component({
  selector: 'state',
  standalone: true,
  imports: [CommonModule, StateItemComponent],
  templateUrl: './states.component.html',
  styleUrl: './states.component.css'
})
export class StatesComponent implements OnInit {

  items: Pair[] = [];

  constructor(private dataService: DataService) {}

  ngOnInit(): void {
    // query for states 
    this.dataService.getStates().subscribe((data: Pair[]) => {
      this.items = data;
    });
  }
}