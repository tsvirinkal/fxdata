import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { Day } from '../models/day.model';
import { Pair } from '../models/pair.model';
import { environment } from '../../environments/environment';
import { Result } from '../models/result.model';
import { Trade } from '../models/trade.model';

var offset = new Date().getTimezoneOffset();
var tzoffset = "?tzo=" + offset;

@Injectable({
  providedIn: 'root',
})
export class DataService {

  constructor(private http: HttpClient) { }

  getData(): Observable<Day[]> {
    return this.http.get<any>(environment.apiUrl + tzoffset); 
  }

  getDataForPair(pair: string): Observable<Day[]> {
    return this.http.get<any>(environment.apiUrl + pair + tzoffset); 
  }

  getStates(): Observable<Pair[]> {
    return this.http.get<any>(environment.apiUrl + "states" + tzoffset); 
  }

  getResults(): Observable<Result[]> {
    return this.http.get<any>(environment.apiUrl + "results" + tzoffset); 
  }

  getTrades(): Observable<Trade[]> {
    return this.http.get<any>(environment.apiUrl + "trades/all" + tzoffset); 
  }

  closeTrade(id: number) {
    this.http.post(environment.apiUrl + "trade/close", {"id": id}).subscribe(
      (response) => {
        console.log('POST response:', response);
      },
      (error) => {
        console.error('POST error:', error);
      });
  }
  
  setActiveState(pair: string, activeTf: string): Observable<any> {
    return this.http.post(environment.apiUrl + "pair/"+pair, {"activeTF": activeTf});
  }
 
  getTodayString(): string {
    const date = new Date(); 
    const day = String(date.getDate()).padStart(2, '0');
    const month = String(date.getMonth() + 1).padStart(2, '0'); 
    const year = date.getFullYear();
    return `${day}.${month}.${year}`;
  }
}
