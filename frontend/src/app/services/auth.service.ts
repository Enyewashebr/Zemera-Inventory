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

 login(username: string, password: string) {
  return this.http.post<any>(`${this.API_URL}/login`, { username, password })
    .pipe(
      tap(res => {
        // Map snake_case from backend → camelCase frontend
        const auth = {
          id: res.id,
          username: res.username,
          role: res.role,
          branchName: res.branch_name,
          branchId: res.branch_id,
          token: res.token
        };

        localStorage.setItem(this.STORAGE_KEY, JSON.stringify(auth));
        this.authSubject.next(auth);
      })
    );
}





  logout(): void {
    localStorage.removeItem(this.STORAGE_KEY);
    this.authSubject.next(null);
  }

  checkTokenAndLogoutIfExpired(): void {
    if (this.isTokenExpired()) {
      console.log('Token expired, logging out...');
      this.logout();
    }
  }

  // ======================
  // AUTH STATE
  // ======================

  isLoggedIn(): boolean {
    return !!this.authSubject.value?.token && !this.isTokenExpired();
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

  getUser(): AuthModel | null {
  return this.authSubject.value;
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

  isTokenExpired(): boolean {
    const token = this.getToken();
    if (!token) return true;

    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      const exp = payload.exp * 1000; // Convert to milliseconds
      return Date.now() > exp;
    } catch (e) {
      return true; // If we can't parse the token, consider it expired
    }
  }

  getname(): string | null {
    return this.authSubject.value?.name ?? null;
  }

  // ======================
  // STORAGE
  // ======================

  private loadFromStorage(): AuthModel | null {
    const raw = localStorage.getItem(this.STORAGE_KEY);
    return raw ? JSON.parse(raw) : null;
  }
}
