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

  constructor(private dataService: DataService) {}

  ngOnInit(): void {
    // query for results 
    this.dataService.getResults().subscribe(results => {
      this.results = results;
    });
 }
}
