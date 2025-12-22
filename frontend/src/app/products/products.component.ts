import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-products',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './products.component.html',
  styleUrl: './products.component.css'
})
export class ProductsComponent {
  productCategories = ['POS Supplies', 'Hardware', 'Software', 'Services'];
  productUnits = ['pcs', 'units', 'box', 'kg', 'liters'];

  products = [
    {
      id: 1,
      name: 'Thermal Paper Rolls',
      category: 'POS Supplies',
      unit: 'pcs',
      buyingPrice: 0.9,
      sellingPrice: 1.8,
      stock: 120,
      createdAt: new Date()
    },
    {
      id: 2,
      name: 'Barcode Labels 4x6',
      category: 'POS Supplies',
      unit: 'pcs',
      buyingPrice: 5.25,
      sellingPrice: 8.5,
      stock: 320,
      createdAt: new Date()
    },
    {
      id: 3,
      name: 'POS Receipt Printer',
      category: 'Hardware',
      unit: 'units',
      buyingPrice: 135,
      sellingPrice: 210,
      stock: 14,
      createdAt: new Date()
    }
  ];

  productForm = {
    name: '',
    category: '',
    unit: '',
    buyingPrice: 0,
    sellingPrice: 0,
    initialStock: 0
  };

  editingProductId: number | null = null;
  formErrors: Record<string, string> = {};

  startEdit(productId: number): void {
    const product = this.products.find((p) => p.id === productId);
    if (!product) return;

    this.editingProductId = productId;
    this.productForm = {
      name: product.name,
      category: product.category,
      unit: product.unit,
      buyingPrice: product.buyingPrice,
      sellingPrice: product.sellingPrice,
      initialStock: product.stock
    };
    this.formErrors = {};
  }

  resetForm(): void {
    this.editingProductId = null;
    this.productForm = {
      name: '',
      category: '',
      unit: '',
      buyingPrice: 0,
      sellingPrice: 0,
      initialStock: 0
    };
    this.formErrors = {};
  }

  submitProduct(): void {
    this.formErrors = {};

    const name = this.productForm.name.trim();
    const category = this.productForm.category;
    const unit = this.productForm.unit;
    const buyingPrice = Number(this.productForm.buyingPrice);
    const sellingPrice = Number(this.productForm.sellingPrice);
    const initialStock = Number(this.productForm.initialStock);

    if (!name) {
      this.formErrors['name'] = 'Product name is required.';
    }
    if (this.isNameTaken(name, this.editingProductId)) {
      this.formErrors['name'] = 'Product name must be unique.';
    }
    if (!category) {
      this.formErrors['category'] = 'Category is required.';
    }
    if (!unit) {
      this.formErrors['unit'] = 'Unit is required.';
    }
    if (Number.isNaN(buyingPrice) || buyingPrice < 0) {
      this.formErrors['buyingPrice'] = 'Buying price must be zero or higher.';
    }
    if (Number.isNaN(sellingPrice) || sellingPrice <= 0) {
      this.formErrors['sellingPrice'] = 'Selling price must be greater than zero.';
    }
    if (this.editingProductId === null) {
      if (Number.isNaN(initialStock) || initialStock < 0) {
        this.formErrors['initialStock'] = 'Initial stock must be zero or higher.';
      }
    }

    if (Object.keys(this.formErrors).length > 0) {
      return;
    }

    if (this.editingProductId !== null) {
      this.products = this.products.map((p) =>
        p.id === this.editingProductId
          ? {
              ...p,
              name,
              category,
              sellingPrice,
              buyingPrice,
              unit: p.unit
            }
          : p
      );
    } else {
      const newProduct = {
        id: Date.now(),
        name,
        category,
        unit,
        buyingPrice,
        sellingPrice,
        stock: initialStock,
        createdAt: new Date()
      };
      this.products = [newProduct, ...this.products];
    }

    this.resetForm();
  }

  get editingProduct() {
    if (this.editingProductId === null) {
      return null;
    }
    return this.products.find((p) => p.id === this.editingProductId) ?? null;
  }

  private isNameTaken(name: string, ignoreId: number | null): boolean {
    const normalized = name.toLowerCase();
    return this.products.some(
      (p) => p.name.toLowerCase() === normalized && (ignoreId === null || p.id !== ignoreId)
    );
  }
}


