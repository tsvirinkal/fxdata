import { Component, OnInit } from '@angular/core';
import { DataService } from '../../services/data.service';
import { Result } from '../../models/result.model';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'results',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './results.component.html',
  styleUrl: './results.component.css'
})
export class ResultsComponent implements OnInit {

  results: Result[] = [];
  total = 0;
  count = 0;
  today = 'today';

  constructor(private dataService: DataService) {}

  ngOnInit(): void {
    // query for results 
    this.dataService.getResults().subscribe(results => {
      this.results = results;
      this.total = this.results.reduce((sum, result) => sum + result.profit, 0);
      this.count = results.length;
    });
    this.today = this.dataService.getTodayString();
  }

  isToday(result: Result): any {
    return result.endTime.includes(this.today);
  }
}
