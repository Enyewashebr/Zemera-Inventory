import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';

interface InventoryRow {
  name: string;
  category: string;
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
    { name: 'Thermal Paper Rolls', category: 'POS Supplies', stock: 120, unit: 'pcs' },
    { name: 'Barcode Labels 4x6', category: 'POS Supplies', stock: 18, unit: 'pcs' },
    { name: 'POS Receipt Printer', category: 'Hardware', stock: 0, unit: 'units' }
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


