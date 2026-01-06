import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { AuthService } from '../services/auth.service';

interface Product {
  id: number;
  name: string;
  unit: string;
}
interface Branch {
  id: number;
  name: string;
}
interface Purchase {
  id: number;
  purchaseDate: string;
  productId: number;
  productName?: string;
  quantity: number;
  unitPrice: number;
  totalCost: number;
  status: 'PENDING' | 'APPROVED' | 'DECLINED';
  approvedBy?: string;
}

@Component({
  selector: 'app-purchases',
  standalone: true,
  imports: [CommonModule, FormsModule, HttpClientModule],
  templateUrl: './purchases.component.html',
  styleUrls: ['./purchases.component.css']
})
export class PurchasesComponent implements OnInit {

  products: Product[] = [];
  purchases: Purchase[] = [];

  branches: Branch[] = [];
selectedBranchId?: number;

  isBranchManager = false;
  isSuperManager = false;

  form = {
    productId: '',
    quantity: 1,
    unitPrice: 0,
    purchaseDate: new Date().toISOString().split('T')[0]
  };

  formErrors: any = {};
  totalCost = 0;
  successMessage = '';
  isSaving = false;

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) {}

 ngOnInit(): void {
  const role = this.authService.getUserRole();

  this.isBranchManager = role === 'BRANCH_MANAGER';
  this.isSuperManager = role === 'SUPER_MANAGER';

  this.loadProducts();

  if (this.isBranchManager) {
    this.loadMyPurchases();
  }

  if (this.isSuperManager) {
    this.loadBranches();
  }
}


  loadProducts() {
    this.http.get<Product[]>('http://localhost:8080/api/products')
      .subscribe(data => this.products = data);
  }
loadMyPurchases() {
  const token = localStorage.getItem('token');

  if (!token) {
    console.error('JWT token missing');
    return;
  }

  this.http.get<Purchase[]>(
    'http://localhost:8080/api/purchase/my',
    {
      headers: {
        Authorization: `Bearer ${token}`
      }
    }
  ).subscribe({
    next: data => this.purchases = data,
    error: err => console.error(err)
  });
}


  loadPurchases() {
    this.http.get<Purchase[]>('http://localhost:8080/api/purchase/getAll')
      .subscribe(data => this.purchases = data);
  }

 

  calculateTotal() {
    this.totalCost = Number(this.form.quantity) * Number(this.form.unitPrice);
  }

  validateForm(): boolean {
    this.formErrors = {};
    let valid = true;

    if (!this.form.productId) {
      this.formErrors.productId = 'Product is required';
      valid = false;
    }
    if (this.form.quantity <= 0) {
      this.formErrors.quantity = 'Quantity must be > 0';
      valid = false;
    }
    if (this.form.unitPrice < 0) {
      this.formErrors.unitPrice = 'Invalid price';
      valid = false;
    }
    return valid;
  }

  savePurchase() {
  if (!this.validateForm()) return;

  const payload = {
    productId: Number(this.form.productId),
    quantity: Number(this.form.quantity),
    unitPrice: Number(this.form.unitPrice),
    totalCost: this.totalCost,
    purchaseDate: this.form.purchaseDate,
    status: 'PENDING'
  };

  const token = localStorage.getItem('token'); // ✅ get JWT from local storage
  if (!token) {
    console.error('No JWT found in localStorage!');
    return;
  }

  this.isSaving = true;

  this.http.post('http://localhost:8080/api/purchase/create', payload, {
    headers: {
      Authorization: `Bearer ${token}`, // ✅ attach JWT
      'Content-Type': 'application/json'
    }
  }).subscribe({
    next: () => {
      this.successMessage = 'Purchase saved (Pending approval)';
      this.clearForm();
      this.loadPurchases();
      this.isSaving = false;
      setTimeout(() => this.successMessage = '', 3000);
    },
    error: err => {
      console.error('Error saving purchase:', err); // now will see 401 or 400 clearly
      this.isSaving = false;
    }
  });
}






  // ==========================
  // SUPER MANAGER
  // ==========================
  loadBranches() {
    this.http.get<Branch[]>('http://localhost:8080/api/branches')
      .subscribe(data => this.branches = data);
  }

  onBranchChange() {
  if (!this.selectedBranchId) return;

  this.http.get<Purchase[]>(
    `http://localhost:8080/api/purchase/branch/${this.selectedBranchId}`
  ).subscribe(data => this.purchases = data);
}


 approvePurchase(id: number) {
  const token = localStorage.getItem('token');
  this.http.put(
    `http://localhost:8080/api/purchase/${id}/approve`,
    {},
    { headers: { Authorization: `Bearer ${token}` } }
  ).subscribe(() => this.onBranchChange());
}


  declinePurchase(id: number) {
  const token = localStorage.getItem('token');
  this.http.put(
    `http://localhost:8080/api/purchase/${id}/approve`,
    {},
    { headers: { Authorization: `Bearer ${token}` } }
  ).subscribe(() => this.onBranchChange());
}

  

  // approvePurchase(id: number) {
  //   this.http.put(`http://localhost:8080/api/purchase/${id}/approve`, {})
  //     .subscribe(() => this.loadPurchases());
  // }

  // declinePurchase(id: number) {
  //   this.http.put(`http://localhost:8080/api/purchase/${id}/decline`, {
  //     comment: 'Declined by super manager'
  //   }).subscribe(() => this.loadPurchases());
  // }

  clearForm() {
    this.form = {
      productId: '',
      quantity: 1,
      unitPrice: 0,
      purchaseDate: new Date().toISOString().split('T')[0]
    };
    this.totalCost = 0;
    this.formErrors = {};
  }
}
