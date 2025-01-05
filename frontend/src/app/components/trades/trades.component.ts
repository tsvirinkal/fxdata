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
  isDeleteMode = false;

  constructor(private dataService: DataService) {}

  ngOnInit(): void {
    // query for trades 
    this.dataService.getTrades().subscribe(trades => {
      this.trades = trades;
      this.calculateTotals();
    });
    this.today = this.dataService.getTodayString();
  }

  isToday(trade: Trade): any {
    return trade.openedTime!=null && trade.openedTime.includes(this.today);
  }

  isError(trade: Trade): any {
    return trade.error != null;
  }

  isBuy(trade: Trade): any {
    return trade.action=='Buy';
  }

  isSell(trade: Trade): any {
    return trade.action=='Sell';
  }

  isPositive(trade: Trade): any {
    return trade.profit>0 && !this.isError(trade);
  }

  isNegative(trade: Trade): any {
    return trade.profit<0 && !this.isError(trade);
  }
  
  onClose(trade: Trade) {
    this.dataService.closeTrade(trade.id);
    this.trades = this.trades.filter(tr => tr !== trade);
    this.calculateTotals();
  }

  toggleDeleteMode(event: Event) {
    const inputElement = event.target as HTMLInputElement;
    this.isDeleteMode = inputElement.checked;
  }

  calculateTotals() {
    this.total = this.trades.reduce((sum, trade) => sum + trade.profit, 0);
    this.count = this.trades.length;
  }
}