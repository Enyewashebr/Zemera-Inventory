import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';

interface Category {
  id: string;
  name: string;
  sellableDefault: boolean;
  subcategories: string[];
  allowedUnits: string[];
}

interface Product {
  id: number;
  name: string;
  categoryId: string;
  categoryName: string;
  subcategory?: string;
  unit: string;
  buyingPrice: number;
  sellingPrice: number;
  stock: number;
  sellable: boolean;
  createdAt: Date;
}

@Component({
  selector: 'app-products',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './products.component.html',
  styleUrl: './products.component.css'
})
export class ProductsComponent {
  categories: Category[] = [
    {
      id: 'cleaning',
      name: 'Cleaning Materials',
      sellableDefault: false,
      subcategories: ['Detergent', 'Bleach', 'Soap', 'Tissue', 'Floor cleaner'],
      allowedUnits: ['L', 'kg', 'pcs']
    },
    {
      id: 'beer',
      name: 'Beer',
      sellableDefault: true,
      subcategories: ['Dashen', 'St. George', 'Harar', 'Waliya', 'Habesha', 'Bedele'],
      allowedUnits: ['pcs'] // bottle
    },
    {
      id: 'water',
      name: 'Water',
      sellableDefault: true,
      subcategories: ['0.25 L', '0.5 L', '1 L', '2 L'],
      allowedUnits: ['pcs']
    },
    {
      id: 'ingredients',
      name: 'Food Ingredients',
      sellableDefault: false,
      subcategories: ['Injera', 'Cabbage', 'Onion', 'Salt', 'Oil', 'Berbere'],
      allowedUnits: ['kg', 'pcs', 'plate']
    },
    {
      id: 'traditional-drinks',
      name: 'Traditional Drinks',
      sellableDefault: true,
      subcategories: ['Tela', 'Tej', 'Areke Dagusa', 'Areke Gibto', 'Areke Normal', 'Areke Nabira'],
      allowedUnits: ['L', 'glass', 'shot']
    },
    {
      id: 'soft-drinks',
      name: 'Soft Drinks',
      sellableDefault: true,
      subcategories: ['Coca-Cola', 'Sprite', 'Fanta', 'Mirinda'],
      allowedUnits: ['pcs']
    },
    {
      id: 'foods-for-sale',
      name: 'Foods for Sale',
      sellableDefault: true,
      subcategories: [
        'Tibs',
        'Tere Siga',
        'Dulet',
        'Kuanta Firfir',
        'Shiro',
        'Tegabino',
        'Timatim Lebleb',
        'Special Shiro',
        'Telba',
        'Firfir'
      ],
      allowedUnits: ['kg', 'frfr', 'plate', 'bowl']
    }
  ];

  products: Product[] = [
    {
      id: 1,
      name: 'Dashen Beer',
      categoryId: 'beer',
      categoryName: 'Beer',
      subcategory: 'Dashen',
      unit: 'pcs',
      buyingPrice: 40,
      sellingPrice: 60,
      stock: 24,
      sellable: true,
      createdAt: new Date()
    },
    {
      id: 2,
      name: 'Water 0.5L',
      categoryId: 'water',
      categoryName: 'Water',
      subcategory: '0.5 L',
      unit: 'pcs',
      buyingPrice: 10,
      sellingPrice: 20,
      stock: 30,
      sellable: true,
      createdAt: new Date()
    },
    {
      id: 3,
      name: 'Floor Cleaner',
      categoryId: 'cleaning',
      categoryName: 'Cleaning Materials',
      subcategory: 'Floor cleaner',
      unit: 'L',
      buyingPrice: 150,
      sellingPrice: 0,
      stock: 5,
      sellable: false,
      createdAt: new Date()
    },
    {
      id: 4,
      name: 'Tibs',
      categoryId: 'foods-for-sale',
      categoryName: 'Foods for Sale',
      subcategory: 'Tibs',
      unit: 'kg',
      buyingPrice: 800,
      sellingPrice: 1200,
      stock: 6.5,
      sellable: true,
      createdAt: new Date()
    }
  ];

  productForm = {
    name: '',
    categoryId: '',
    subcategory: '',
    unit: '',
    buyingPrice: 0,
    sellingPrice: 0,
    initialStock: 0,
    sellable: true
  };

  editingProductId: number | null = null;
  formErrors: Record<string, string> = {};
  private nextId = 1000;

  get selectedCategory(): Category | undefined {
    return this.categories.find((c) => c.id === this.productForm.categoryId);
  }

  get availableSubcategories(): string[] {
    return this.selectedCategory?.subcategories ?? [];
  }

  get availableUnits(): string[] {
    return this.selectedCategory?.allowedUnits ?? [];
  }

  startEdit(productId: number): void {
    const product = this.products.find((p) => p.id === productId);
    if (!product) return;

    this.editingProductId = productId;
    this.productForm = {
      name: product.name,
      categoryId: product.categoryId,
      subcategory: product.subcategory ?? '',
      unit: product.unit,
      buyingPrice: product.buyingPrice,
      sellingPrice: product.sellingPrice,
      initialStock: product.stock,
      sellable: product.sellable
    };
    this.formErrors = {};
  }

  resetForm(): void {
    this.editingProductId = null;
    this.productForm = {
      name: '',
      categoryId: '',
      subcategory: '',
      unit: '',
      buyingPrice: 0,
      sellingPrice: 0,
      initialStock: 0,
      sellable: true
    };
    this.formErrors = {};
  }

  submitProduct(): void {
    this.formErrors = {};

    const name = this.productForm.name.trim();
    const categoryId = this.productForm.categoryId;
    const category = this.categories.find((c) => c.id === categoryId);
    const subcategory = this.productForm.subcategory;
    const unit = this.productForm.unit;
    const buyingPrice = Number(this.productForm.buyingPrice);
    const sellingPrice = Number(this.productForm.sellingPrice);
    const initialStock = Number(this.productForm.initialStock);
    let sellable = this.productForm.sellable;

    if (!name) {
      this.formErrors['name'] = 'Product name is required.';
    }
    if (this.isNameTaken(name, this.editingProductId)) {
      this.formErrors['name'] = 'Product name must be unique.';
    }
    if (!categoryId || !category) {
      this.formErrors['category'] = 'Category is required.';
    }
    if (!subcategory && category && category.subcategories.length > 0) {
      this.formErrors['subcategory'] = 'Subcategory is required for this category.';
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
    // Cleaning materials are always non-sellable.
    if (category && category.id === 'cleaning') {
      sellable = false;
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
              categoryId,
              categoryName: category?.name ?? p.categoryName,
              subcategory,
              sellingPrice,
              buyingPrice,
              unit: p.unit,
              sellable
            }
          : p
      );
    } else {
      const newProduct = {
        id: this.nextId++,
        name,
        categoryId,
        categoryName: category?.name ?? '',
        subcategory,
        unit,
        buyingPrice,
        sellingPrice,
        stock: initialStock,
        sellable,
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


