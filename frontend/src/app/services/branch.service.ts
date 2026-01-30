import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Branch } from '../model/branch.model';

@Injectable({
  providedIn: 'root'
})
export class BranchService {
  private baseUrl = 'https://zemera-inventory-1.onrender.com/api/branches';

  constructor(private http: HttpClient) {}

  getAll(): Observable<Branch[]> {
    return this.http.get<Branch[]>(this.baseUrl);
  }

  create(branch: Branch): Observable<Branch> {
    return this.http.post<Branch>(this.baseUrl, branch);
  }
  getAllBranches(): Observable<Branch[]> {
    return this.http.get<Branch[]>(this.baseUrl);
  }
// update(id: number, branch: Branch): Observable<Branch> {
//     return this.http.put<Branch>(`${this.baseUrl}/${id}`, branch);
//   }

//   delete(id: number): Observable<void> {
//     return this.http.delete<void>(`${this.baseUrl}/${id}`);
//   }

}
  



