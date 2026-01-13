import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { StockView } from '../model/stockView.model';
import { AuthService } from './auth.service';

@Injectable({ providedIn: 'root' })
export class StockService {
  private baseUrl = 'http://localhost:8080/api/stock';

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) {}

  private headers(): HttpHeaders {
    return new HttpHeaders({
      Authorization: `Bearer ${this.authService.getToken()}`
    });
  }

  getMyStock(): Observable<StockView[]> {
    return this.http.get<StockView[]>(
      `${this.baseUrl}/my`,
      { headers: this.headers() }
    );
  }

  getByBranch(branchId: number): Observable<StockView[]> {
    return this.http.get<StockView[]>(
      `${this.baseUrl}/branch/${branchId}`,
      { headers: this.headers() }
    );
  }
}
