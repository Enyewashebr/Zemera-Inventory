import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { AuthModel } from '../model/auth.model';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private API_URL = 'http://localhost:8080/api/auth';

  private readonly STORAGE_KEY = 'auth_session';

  private authSubject = new BehaviorSubject<AuthModel | null>(
    this.loadFromStorage()
  );

  auth$ = this.authSubject.asObservable();

  constructor(private http: HttpClient) {}

  // ======================
  // AUTH ACTIONS
  // ======================

 login(username: string, password: string): Observable<AuthModel> {
  return this.http.post<AuthModel>(`${this.API_URL}/login`, { username, password })
    .pipe(
      tap(auth => {
        localStorage.setItem(this.STORAGE_KEY, JSON.stringify(auth));
        this.authSubject.next(auth);
      })
    );
}



  logout(): void {
    localStorage.removeItem(this.STORAGE_KEY);
    this.authSubject.next(null);
  }

  // ======================
  // AUTH STATE
  // ======================

  isLoggedIn(): boolean {
    return !!this.authSubject.value?.token;
  }

  isAdmin(): boolean {
    return this.authSubject.value?.role === 'SUPER_MANAGER';
  }

  // ======================
  // USER INFO (USED BY LAYOUT)
  // ======================

  getUserName(): string {
    return this.authSubject.value?.username ?? 'User';
  }

  getUserRole(): string {
    return this.authSubject.value?.role ?? 'UNKNOWN';
  }
  getDashboardRoute(): string {
  const role = this.getUserRole();
  if (role === 'SUPER_MANAGER') return '/dashboard/super';
  return '/dashboard/branch'; // default branch manager
}


  getUserInitials(): string {
    const name = this.getUserName();
    return name.charAt(0).toUpperCase();
  }

  // ======================
  // TOKEN & BRANCH
  // ======================

  getToken(): string | null {
    return this.authSubject.value?.token ?? null;
  }

  getBranchId(): number | null {
    return this.authSubject.value?.branchId ?? null;
  }

  // ======================
  // STORAGE
  // ======================

  private loadFromStorage(): AuthModel | null {
    const raw = localStorage.getItem(this.STORAGE_KEY);
    return raw ? JSON.parse(raw) : null;
  }
}
