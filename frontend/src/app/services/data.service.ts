import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { Day } from '../models/day.model';
import { Pair } from '../models/pair.model';
import { environment } from '../../environments/environment';

var offset = new Date().getTimezoneOffset();
var tzoffset = "?tzo=" + offset;

@Injectable({
  providedIn: 'root',
})
export class DataService {
  
  constructor(private http: HttpClient) { }

  getData(): Observable<Day[]> {
    console.log(environment.apiUrl);
    return this.http.get<any>(environment.apiUrl + tzoffset); 
  }

  getStates(): Observable<Pair[]> {
    console.log(environment.apiUrl);
    return this.http.get<any>(environment.apiUrl + "states/" + tzoffset); 
  }
}
