import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from './auth.service';

@Injectable({ providedIn: 'root' })
export class SalesService {

  private baseUrl = 'http://localhost:8080/api/reports';

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) {}

  private headers(): HttpHeaders {
    return new HttpHeaders({
      Authorization: `Bearer ${this.authService.getToken()}`
    });
  }

 getReport(
  time: string,
  type: string,
  value: string,
  branchId?: number, // ✅ optional
): Observable<any> {

  let params = new HttpParams()
    .set('time', time)
    .set('type', type)
    .set('value', value);

  if (branchId !== undefined) {
    params = params.set('branchId', branchId.toString());
  }

  return this.http.get<any>(this.baseUrl, {
    headers: this.headers(),
    params
  });
}
}
