import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { Day } from '../models/day.model';

@Injectable({
  providedIn: 'root',
})
export class DataService {
  private apiUrl = 'https://tsv.ddns.net:7443/api/v2/fxdata/';  

  constructor(private http: HttpClient) { }

  getData(): Observable<Day[]> {
    return this.http.get<any>(this.apiUrl); 
  }
}
