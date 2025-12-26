import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { map } from 'rxjs/operators';

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
  imports: [CommonModule, FormsModule, HttpClientModule],
  templateUrl: './products.component.html',
  styleUrls: ['./products.component.css']
})
export class ProductsComponent {
  constructor(private http: HttpClient) {}

  backendUrl = 'http://localhost:8080/api/products';

  categories: Category[] = [
    { id: 'cleaning', name: 'Cleaning Materials', sellableDefault: false, subcategories: ['Detergent', 'Bleach', 'Soap', 'Tissue', 'Floor cleaner'], allowedUnits: ['L', 'kg', 'pcs'] },
    { id: 'beer', name: 'Beer', sellableDefault: true, subcategories: ['Dashen', 'St. George', 'Harar', 'Waliya', 'Habesha', 'Bedele'], allowedUnits: ['pcs'] },
    { id: 'water', name: 'Water', sellableDefault: true, subcategories: ['0.25 L', '0.5 L', '1 L', '2 L'], allowedUnits: ['pcs'] },
    { id: 'ingredients', name: 'Food Ingredients', sellableDefault: false, subcategories: ['Injera', 'Cabbage', 'Onion', 'Salt', 'Oil', 'Berbere'], allowedUnits: ['kg', 'pcs', 'plate'] },
    { id: 'traditional-drinks', name: 'Traditional Drinks', sellableDefault: true, subcategories: ['Tela', 'Tej', 'Areke Dagusa', 'Areke Gibto', 'Areke Normal', 'Areke Nabira'], allowedUnits: ['L', 'glass', 'shot'] },
    { id: 'soft-drinks', name: 'Soft Drinks', sellableDefault: true, subcategories: ['Coca-Cola', 'Sprite', 'Fanta', 'Mirinda'], allowedUnits: ['pcs'] },
    { id: 'foods-for-sale', name: 'Foods for Sale', sellableDefault: true, subcategories: ['Tibs','Tere Siga','Dulet','Kuanta Firfir','Shiro','Tegabino','Timatim Lebleb','Special Shiro','Telba','Firfir'], allowedUnits: ['kg','frfr','plate','bowl'] }
  ];

  products: Product[] = [];

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

  get selectedCategory(): Category | undefined {
    return this.categories.find(c => c.id === this.productForm.categoryId);
  }

  get availableSubcategories(): string[] {
    return this.selectedCategory?.subcategories ?? [];
  }

  get availableUnits(): string[] {
    return this.selectedCategory?.allowedUnits ?? [];
  }

  startEdit(productId: number): void {
    const product = this.products.find(p => p.id === productId);
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
    const category = this.categories.find(c => c.id === categoryId);
    const subcategory = this.productForm.subcategory;
    const unit = this.productForm.unit;
    const buyingPrice = Number(this.productForm.buyingPrice);
    const sellingPrice = Number(this.productForm.sellingPrice);
    const initialStock = Number(this.productForm.initialStock);
    let sellable = this.productForm.sellable;

    if (!name) this.formErrors['name'] = 'Product name is required.';
    if (!categoryId || !category) this.formErrors['category'] = 'Category is required.';
    if (!subcategory && category && category.subcategories.length > 0) this.formErrors['subcategory'] = 'Subcategory is required.';
    if (!unit) this.formErrors['unit'] = 'Unit is required.';
    if (Number.isNaN(buyingPrice) || buyingPrice < 0) this.formErrors['buyingPrice'] = 'Buying price must be zero or higher.';
    if (Number.isNaN(sellingPrice) || sellingPrice <= 0) this.formErrors['sellingPrice'] = 'Selling price must be greater than zero.';
    if (Number.isNaN(initialStock) || initialStock < 0) this.formErrors['initialStock'] = 'Initial stock must be zero or higher.';
    if (category && category.id === 'cleaning') sellable = false;

    if (Object.keys(this.formErrors).length > 0) return;

    const payload = {
      name,
      categoryId,
      subcategory,
      unit,
      buyingPrice,
      sellingPrice,
      stock: initialStock,
      sellable
    };

    if (this.editingProductId !== null) {
  this.http
    .put<Product>(
      `${this.backendUrl}/${this.editingProductId}`,
      {
        ...payload,
        // IMPORTANT: backend expects stock, not initialStock
        stock: this.editingProduct?.stock
      }
    )
    .pipe(
      map(p => ({
        ...p,
        createdAt: new Date(p.createdAt),
        categoryName: this.categories.find(c => c.id === p.categoryId)?.name ?? ''
      }))
    )
    .subscribe({
      next: (updatedProduct) => {
        this.products = this.products.map(p =>
          p.id === updatedProduct.id ? updatedProduct : p
        );
        this.resetForm();
      },
      error: err => {
        console.error('Failed to update product', err);
      }
    });

  return;
}


    // POST new product to backend
    this.http.post<Product>(this.backendUrl, payload)
      .pipe(
        map(p => ({
          ...p,
          createdAt: new Date(p.createdAt),          // convert string to Date
          categoryName: this.categories.find(c => c.id === p.categoryId)?.name ?? ''
        }))
      )
      .subscribe({
        next: (newProduct) => {
          this.products = [newProduct, ...this.products];
          this.resetForm();
        },
        error: (err) => {
          console.error('Failed to add product', err);
        }
      });
  }

  get editingProduct() {
    if (this.editingProductId === null) return null;
    return this.products.find(p => p.id === this.editingProductId) ?? null;
  }
}
