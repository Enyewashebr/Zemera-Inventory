import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';

interface InventoryRow {
  name: string;
  category: string;
  subcategory?: string;
  stock: number;
  unit: string;
}

@Component({
  selector: 'app-inventory',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './inventory.component.html',
  styleUrl: './inventory.component.css'
})
export class InventoryComponent {
  rows: InventoryRow[] = [
    { name: 'Dashen Beer', category: 'Beer', subcategory: 'Dashen', stock: 24, unit: 'pcs' },
    { name: 'Harar Beer', category: 'Beer', subcategory: 'Harar', stock: 18, unit: 'pcs' },
    { name: 'Water 0.5L', category: 'Water', subcategory: '0.5 L', stock: 30, unit: 'pcs' },
    { name: 'Water 1L', category: 'Water', subcategory: '1 L', stock: 12, unit: 'pcs' },
    { name: 'Areke Dagusa', category: 'Traditional Drinks', subcategory: 'Dagusa', stock: 5, unit: 'L' },
    { name: 'Areke Gibto', category: 'Traditional Drinks', subcategory: 'Gibto', stock: 3, unit: 'L' },
    { name: 'Floor Cleaner', category: 'Cleaning Materials', subcategory: 'Floor cleaner', stock: 5, unit: 'L' }
  ];

  search = '';
  categoryFilter = '';

  get categories(): string[] {
    return Array.from(new Set(this.rows.map((r) => r.category)));
  }

  filteredRows(): InventoryRow[] {
    const term = this.search.toLowerCase();
    return this.rows.filter((row) => {
      const matchesCategory = this.categoryFilter ? row.category === this.categoryFilter : true;
      const matchesSearch =
        !term ||
        row.name.toLowerCase().includes(term) ||
        row.category.toLowerCase().includes(term);
      return matchesCategory && matchesSearch;
    });
  }

  status(row: InventoryRow): 'normal' | 'low' | 'out' {
    if (row.stock <= 0) {
      return 'out';
    }
    if (row.stock < 20) {
      return 'low';
    }
    return 'normal';
  }
}


