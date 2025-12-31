import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-branch-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './branch-dashboard.component.html',
  styleUrl: './branch-dashboard.component.css'
})
export class BranchDashboardComponent implements OnInit, OnDestroy {

  stats = [
    { label: 'Products', value: '1,240' },
    { label: 'Orders', value: '68' },
    { label: 'Inventory Health', value: '96%' }
  ];

  metrics = [
    { label: 'Sales Today', value: 18250, unit: 'ETB' },
    { label: 'Orders Today', value: 68, unit: 'orders' }
  ];

  private timer?: ReturnType<typeof setInterval>;

  ngOnInit(): void {
    this.timer = setInterval(() => {
      this.metrics[0].value += Math.floor(Math.random() * 300);
    }, 7000);
  }

  ngOnDestroy(): void {
    if (this.timer) clearInterval(this.timer);
  }
}
