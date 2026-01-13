import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from './auth.service'; // make sure this exists

/* =======================
   CREATE ORDER PAYLOADS
======================= */

export interface CreateOrderItemPayload {
  productId?: number;
  productName: string;
  quantity: number;
  unit: string;
  unitPrice: number;
}

export interface CreateOrderPayload {
  waiterName: string;
  items: CreateOrderItemPayload[];
}

/* =======================
   ORDER / SALES RESPONSES
======================= */

export interface OrderRow {
  id: number;
  productName: string;
  quantity: number;
  unit: string;
  unitPrice: number;
  lineTotal: number;
  waiterName: string;
  createdAt: string;
}

export interface OrderTicketItem {
  productName: string;
  quantity: number;
  unit: string;
  unitPrice: number;
  lineTotal: number;
}

export interface OrderTicketResponse {
  orderId: number;
  waiterName: string;
  createdAt: string;
  currency: string; // "ETB"
  totalAmount: number;
  items: OrderTicketItem[];
}

/* =======================
   SERVICE
======================= */

@Injectable({
  providedIn: 'root'
})
export class OrderService {
  private readonly baseUrl = 'http://localhost:8080/api/orders';

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) {}

  /** Helper: attach JWT headers */
  private getAuthHeaders(): HttpHeaders {
    const token = this.authService.getToken();
    if (!token) {
      console.error('JWT token missing');
      throw new Error('JWT token missing');
    }

    return new HttpHeaders({
      Authorization: `Bearer ${token}`,
      'Content-Type': 'application/json'
    });
  }

  /**
   * Create a new order
   * - Backend deducts stock for sellable items
   * - Kitchen items are ignored by stock logic
   */
  createOrder(payload: CreateOrderPayload): Observable<OrderTicketResponse> {
    const headers = this.getAuthHeaders();
    return this.http.post<OrderTicketResponse>(this.baseUrl, payload, { headers });
  }

  /**
   * Get orders (sales) by date – Reports → Daily sales
   */
  getOrdersByDate(date: string): Observable<OrderRow[]> {
    const headers = this.getAuthHeaders();
    const params = new HttpParams().set('date', date);
    return this.http.get<OrderRow[]>(this.baseUrl, { headers, params });
  }

  /**
   * Get monthly orders – Reports → Monthly
   */
  getOrdersByMonth(month: string): Observable<OrderRow[]> {
    const headers = this.getAuthHeaders();
    const params = new HttpParams().set('month', month);
    return this.http.get<OrderRow[]>(`${this.baseUrl}/monthly`, { headers, params });
  }

  /**
   * Get single order ticket (optional – reprint)
   */
  getOrderTicket(orderId: number): Observable<OrderTicketResponse> {
    const headers = this.getAuthHeaders();
    return this.http.get<OrderTicketResponse>(`${this.baseUrl}/${orderId}`, { headers });
  }
}
