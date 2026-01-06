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
  approvedBy?: number;
  approvedByName?: string;
  branchId?: number;
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
  // this.loadMyPurchases();


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
  // Check if token is expired before making the request
  if (this.authService.isTokenExpired()) {
    console.error('JWT token expired');
    this.authService.logout();
    // You might want to redirect to login page here
    return;
  }

  const token = this.authService.getToken();

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
    error: err => {
      if (err.status === 401) {
        console.error('Unauthorized - token may be invalid');
        this.authService.logout();
        // You might want to redirect to login page here
      } else {
        console.error(err);
      }
    }
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

  // Check if token is expired before making the request
  if (this.authService.isTokenExpired()) {
    console.error('JWT token expired');
    this.authService.logout();
    return;
  }

  const payload = {
    productId: Number(this.form.productId),
    quantity: Number(this.form.quantity),
    unitPrice: Number(this.form.unitPrice),
    totalCost: this.totalCost,
    purchaseDate: this.form.purchaseDate,
    status: 'PENDING'
  };

  const token = this.authService.getToken(); // ✅ get JWT from AuthService
  if (!token) {
    console.error('No JWT found!');
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
      // Load appropriate purchases based on user role
      if (this.isBranchManager) {
        this.loadMyPurchases();
      } else if (this.isSuperManager) {
        this.loadPurchases();
      }
      this.isSaving = false;
      setTimeout(() => this.successMessage = '', 3000);
    },
    error: err => {
      if (err.status === 401) {
        console.error('Unauthorized - token may be invalid');
        this.authService.logout();
      } else {
        console.error('Error saving purchase:', err);
      }
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
  if (this.authService.isTokenExpired()) {
    console.error('JWT token expired');
    this.authService.logout();
    return;
  }

  const token = this.authService.getToken();
  if (!token) return;

  this.http.put(
    `http://localhost:8080/api/purchase/${id}/approve`,
    {},
    { headers: { Authorization: `Bearer ${token}` } }
  ).subscribe({
    next: () => this.onBranchChange(),
    error: err => {
      if (err.status === 401) {
        console.error('Unauthorized - token may be invalid');
        this.authService.logout();
      }
    }
  });
}


 declinePurchase(id: number) {
  if (this.authService.isTokenExpired()) {
    console.error('JWT token expired');
    this.authService.logout();
    return;
  }

  const token = this.authService.getToken();
  if (!token) return;

  this.http.put(
    `http://localhost:8080/api/purchase/${id}/decline`,
    { comment: 'Declined by super manager' },
    { headers: { Authorization: `Bearer ${token}` } }
  ).subscribe({
    next: () => this.onBranchChange(),
    error: err => {
      if (err.status === 401) {
        console.error('Unauthorized - token may be invalid');
        this.authService.logout();
      }
    }
  });
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
