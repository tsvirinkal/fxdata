import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { DataService } from '../../services/data.service';
import { Day } from '../../models/day.model';
import { ActionComponent } from '../action/action.component';

@Component({
  selector: 'actions',
  standalone: true,
  imports: [CommonModule, ActionComponent],
  templateUrl: './actions.component.html',
  styleUrls: ['./actions.component.css']
})
export class ActionsComponent implements OnInit {
  items: Day[] = [];
  pair: string | null = null;
  today = 'today'

  constructor(private dataService: DataService) {}

  ngOnInit(): void {
    this.dataService.getData().subscribe((data: Day[]) => {
      this.items = data;
    });
    this.today = this.dataService.getTodayString();
  }

  isToday(result: Day): any {
    return result.date.includes(this.today);
  }
  
  filter(pair: string) {
    this.dataService.getDataForPair(pair).subscribe((data: Day[]) => {
      this.items = data;
    });
    this.pair = pair;
  }
  
  displayAll() {
    this.pair = null;
    this.ngOnInit();
  }
}
