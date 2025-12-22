import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { InventoryService, InventoryItem } from '../services/inventory.service';

@Component({
  selector: 'app-inventory',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './inventory.component.html',
  styleUrl: './inventory.component.css'
})
export class InventoryComponent {
  rows: (InventoryItem & { subcategory?: string })[] = [
    { name: 'Dashen Beer', category: 'Beer', unit: 'pcs', stock: 24, subcategory: 'Dashen' },
    { name: 'Harar Beer', category: 'Beer', unit: 'pcs', stock: 18, subcategory: 'Harar' },
    { name: 'Water 0.5L', category: 'Water', unit: 'pcs', stock: 30, subcategory: '0.5 L' },
    { name: 'Water 1L', category: 'Water', unit: 'pcs', stock: 12, subcategory: '1 L' },
    { name: 'Areke Dagusa', category: 'Traditional Drinks', unit: 'L', stock: 5, subcategory: 'Dagusa' },
    { name: 'Areke Gibto', category: 'Traditional Drinks', unit: 'L', stock: 3, subcategory: 'Gibto' },
    { name: 'Tej Glass', category: 'Traditional Drinks', unit: 'glass', stock: 40, subcategory: 'Glass' }
  ];

  search = '';
  categoryFilter = '';

  constructor(private inventoryService: InventoryService) {
    // Sync initial quantities from the shared service.
    const liveItems = this.inventoryService.getAll();
    this.rows = this.rows.map((row) => {
      const live = liveItems.find((i) => i.name === row.name);
      return live ? { ...row, stock: live.stock } : row;
    });
  }

  get categories(): string[] {
    return Array.from(new Set(this.rows.map((r) => r.category)));
  }

  filteredRows(): (InventoryItem & { subcategory?: string })[] {
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

  status(row: InventoryItem): 'normal' | 'low' | 'out' {
    if (row.stock <= 0) {
      return 'out';
    }
    if (row.stock < 20) {
      return 'low';
    }
    return 'normal';
  }
}


