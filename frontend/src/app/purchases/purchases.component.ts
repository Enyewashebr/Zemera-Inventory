import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
// import { PurchaseService, Purchase } from '../services/purchase.service';
// import { ProductService, Product } from '../services/product.service';
import { AuthService } from '../services/auth.service';
// import { Component } from '@angular/core';
import { CommonModule, CurrencyPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { PurchaseService, Purchase } from '../services/purchase.service';
import { ProductService, Product } from '../services/product.service';

@Component({
  selector: 'app-purchases',
  standalone: true,
  imports: [
    CommonModule,   // NgIf, NgFor, currency pipe
    FormsModule,    // ngModel
    CurrencyPipe
  ],
  templateUrl: './purchases.component.html',
})
export class PurchasesComponent implements OnInit {

  products: Product[] = [];
  purchases: Purchase[] = [];

  form = {
    productId: null as number | null,
    quantity: 1,
    unitPrice: 0,
    date: new Date().toISOString().substring(0, 10),
    branchId: 0
  };

  formErrors: any = {};
  totalCost = 0;
  successMessage = '';
  isSaving = false;

  constructor(
    private productService: ProductService,
    private purchaseService: PurchaseService,
    private auth: AuthService
  ) {}

  ngOnInit(): void {
    const branchId = this.auth.getBranchId();

    if (branchId === null) {
      console.error('No branch assigned to user');
      return;
    }

    this.form.branchId = branchId;

    this.loadProducts();
    this.loadPurchases();
  }

  /* ================= LOAD DATA ================= */

  loadProducts(): void {
    this.productService.getAllProducts().subscribe((res: Product[]) => {
      this.products = res;
    });
  }

  loadPurchases(): void {
    if (!this.form.branchId) return;

    this.purchaseService
      .getByBranch(this.form.branchId)
      .subscribe((res: Purchase[]) => {
        this.purchases = res;
      });
  }

  /* ================= FORM LOGIC ================= */

  onProductChange(): void {
    const product = this.products.find(
      (p: Product) => p.id === this.form.productId
    );

    if (product && !product.sellable) {
      this.form.unitPrice = 0;
    }

    this.calculateTotal();
  }

  calculateTotal(): void {
    this.totalCost =
      (this.form.quantity || 0) * (this.form.unitPrice || 0);
  }

  clearForm(): void {
    this.form = {
      productId: null,
      quantity: 1,
      unitPrice: 0,
      date: new Date().toISOString().substring(0, 10),
      branchId: this.form.branchId
    };

    this.formErrors = {};
    this.totalCost = 0;
    this.successMessage = '';
  }

  /* ================= SAVE PURCHASE ================= */

  savePurchase(): void {
    this.isSaving = true;
    this.formErrors = {};

    if (!this.form.productId)
      this.formErrors.productId = 'Product is required';

    if (!this.form.quantity || this.form.quantity < 1)
      this.formErrors.quantity = 'Quantity must be at least 1';

    if (this.form.unitPrice < 0)
      this.formErrors.unitPrice = 'Unit price cannot be negative';

    if (!this.form.date)
      this.formErrors.date = 'Date is required';

    if (Object.keys(this.formErrors).length) {
      this.isSaving = false;
      return;
    }

    const purchasePayload: Purchase = {
      productId: this.form.productId!,
      quantity: this.form.quantity,
      unitPrice: this.form.unitPrice,
      totalCost: this.totalCost,
      date: this.form.date,
      branchId: this.form.branchId,
      status: 'Pending',
      productName:
        this.products.find(p => p.id === this.form.productId)?.name || ''
    };

    this.purchaseService.create(purchasePayload).subscribe({
      next: () => {
        this.successMessage =
          'Purchase added successfully and pending approval';
        this.loadPurchases();
        this.clearForm();
        this.isSaving = false;
      },
      error: err => {
        console.error(err);
        this.isSaving = false;
      }
    });
  }
}
