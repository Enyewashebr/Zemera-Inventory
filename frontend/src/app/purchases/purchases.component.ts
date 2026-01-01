import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpClientModule } from '@angular/common/http';

interface Product {
  id: number;
  name: string;
  unit: string;
}

interface Purchase {
  date: string;
  productId: number;
  productName: string;
  purchaseDate: number;
  // categoryId: string;
  // subcategory: string;
  quantity: number;
  unitPrice: number;
  totalCost: number;
  status: string;
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

  form = {
    productId: '',
    quantity: 1,
    unitPrice: 0,
    purchaseDate: new Date().toISOString().split('T')[0] // default today
  };
selectedCategoryName = '';
selectedSubcategory = '';



  formErrors: any = {};
  totalCost: number = 0;
  successMessage: string = '';
  isSaving: boolean = false;

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.loadProducts();
    this.loadPurchases();
  }

  loadProducts() {
    this.http.get<Product[]>('http://localhost:8080/api/products')
      .subscribe({
        next: (data) => this.products = data,
        error: (err) => console.error('Error loading products', err)
      });
  }

  loadPurchases() {
    this.http.get<Purchase[]>('http://localhost:8080/api/purchase/getAll')
      .subscribe({
        next: (data) => this.purchases = data,
        error: (err) => console.error('Error loading purchases', err)
      });
  }

  onProductChange() {
    this.calculateTotal();
  }

  calculateTotal() {
    const qty = Number(this.form.quantity);
    const price = Number(this.form.unitPrice);
    this.totalCost = qty && price ? qty * price : 0;
  }

  validateForm(): boolean {
    this.formErrors = {};
    let valid = true;

    if (!this.form.productId) {
      this.formErrors.productId = 'Product is required.';
      valid = false;
    }

    if (!this.form.quantity || this.form.quantity <= 0) {
      this.formErrors.quantity = 'Quantity must be greater than zero.';
      valid = false;
    }

    if (!this.form.unitPrice || this.form.unitPrice < 0) {
      this.formErrors.unitPrice = 'Unit price must be non-negative.';
      valid = false;
    }

    if (!this.form.purchaseDate) {
      this.formErrors.purchaseDate = 'Date is required.';
      valid = false;
    }

    return valid;
  }

  savePurchase() {
    if (!this.validateForm()) return;

    this.isSaving = true;

   const purchasePayload = {
  productId: Number(this.form.productId),   // ✅ force number
  quantity: Number(this.form.quantity),
  unitPrice: Number(this.form.unitPrice),
  purchaseDate: Number(this.form.purchaseDate),
  totalCost: this.totalCost,
  status: 'PENDING',                         // ✅ REQUIRED
  approvedBy: null
};

console.log('Sending purchase payload:', purchasePayload);
    this.http.post('http://localhost:8080/api/purchase/create', purchasePayload)
      .subscribe({
        next: () => {
          this.successMessage = 'Purchase saved successfully!';
          this.clearForm();
          this.loadPurchases();
          this.isSaving = false;
          setTimeout(() => this.successMessage = '', 3000);
        },
        error: (err) => {
          console.error('Error saving purchase', err);
          this.isSaving = false;
        }
      });
  }

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
