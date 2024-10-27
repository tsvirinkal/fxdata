import { Component, OnInit } from '@angular/core';
import { Trade } from '../../models/trade.model';
import { DataService } from '../../services/data.service';
import { CommonModule } from '@angular/common';
@Component({
  selector: 'trades',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './trades.component.html',
  styleUrl: './trades.component.css'
})
export class TradesComponent implements OnInit {

  trades: Trade[] = [];

  constructor(private dataService: DataService) {}

  ngOnInit(): void {
    // query for trades 
    this.dataService.getTrades().subscribe(trades => {
      this.trades = trades;
    });
  }
}