import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { DataService } from '../../services/data.service';
import { Day } from '../../models/day.model';

@Component({
  selector: 'list-group',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './list-group.component.html',
  styleUrls: ['./list-group.component.css']
})
export class ListGroupComponent implements OnInit {

  items: Day[] = [];

  constructor(private dataService: DataService) {}

  ngOnInit(): void {
    this.dataService.getData().subscribe((data: Day[]) => {
      this.items = data;
    });
  }
}
