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
  total = 0;
  count = 0;
  today = 'today';

  constructor(private dataService: DataService) {}

  ngOnInit(): void {
    // query for trades 
    this.dataService.getTrades().subscribe(trades => {
      this.trades = trades;
      this.total = this.trades.reduce((sum, trade) => sum + trade.profit, 0);
      this.count = this.trades.length;
    });
    this.today = this.dataService.getTodayString();
  }

  isToday(trade: Trade): any {
    return trade.openedTime.includes(this.today);
  }
  onClose(trade: Trade) {
    this.dataService.closeTrade(trade.id);
  }
}