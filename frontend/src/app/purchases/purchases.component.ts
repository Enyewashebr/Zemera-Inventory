import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';

interface PurchaseRow {
  id: number;
  date: string;
  product: string;
  quantity: number;
  unit: string;
  unitPrice: number;
  totalCost: number;
}

@Component({
  selector: 'app-purchases',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './purchases.component.html',
  styleUrl: './purchases.component.css'
})
export class PurchasesComponent {
  products = [
    { name: 'Thermal Paper Rolls', unit: 'pcs' },
    { name: 'Barcode Labels 4x6', unit: 'pcs' },
    { name: 'POS Receipt Printer', unit: 'units' }
  ];

  form = {
    product: '',
    quantity: 0,
    unitPrice: 0,
    date: new Date().toISOString().substring(0, 10)
  };

  formErrors: Record<string, string> = {};
  successMessage = '';

  purchases: PurchaseRow[] = [];
  private nextId = 1;

  get selectedProductUnit(): string {
    return this.products.find((p) => p.name === this.form.product)?.unit ?? '';
  }

  get totalCost(): number {
    const q = Number(this.form.quantity);
    const price = Number(this.form.unitPrice);
    if (Number.isNaN(q) || Number.isNaN(price)) return 0;
    return q * price;
  }

  savePurchase(): void {
    this.formErrors = {};
    this.successMessage = '';

    const { product, quantity, unitPrice, date } = this.form;
    const unit = this.selectedProductUnit;

    if (!product) {
      this.formErrors['product'] = 'Product is required.';
    }
    if (!date) {
      this.formErrors['date'] = 'Purchase date is required.';
    }
    if (Number.isNaN(quantity) || quantity <= 0) {
      this.formErrors['quantity'] = 'Quantity must be greater than zero.';
    }
    if (Number.isNaN(unitPrice) || unitPrice <= 0) {
      this.formErrors['unitPrice'] = 'Unit price must be greater than zero.';
    }
    if (!unit) {
      this.formErrors['unit'] = 'Unit cannot be determined from product.';
    }

    if (Object.keys(this.formErrors).length > 0) {
      return;
    }

    const row: PurchaseRow = {
      id: this.nextId++,
      date,
      product,
      quantity,
      unit,
      unitPrice,
      totalCost: this.totalCost
    };

    this.purchases = [row, ...this.purchases];

    // In a real app this is where stock would be increased via a shared store or API.

    this.successMessage = 'Purchase saved and stock updated.';
    this.clearForm();
  }

  clearForm(): void {
    this.form = {
      product: '',
      quantity: 0,
      unitPrice: 0,
      date: new Date().toISOString().substring(0, 10)
    };
    this.formErrors = {};
  }
}





