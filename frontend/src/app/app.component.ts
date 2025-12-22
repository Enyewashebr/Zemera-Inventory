import { Component, OnDestroy, OnInit } from '@angular/core';
import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent implements OnInit, OnDestroy {
  title = 'Zemera Inventory';
  currentDateTime = new Date();
  timer?: ReturnType<typeof setInterval>;

  navItems = [
    'Dashboard',
    'Products',
    'Purchases',
    'Orders',
    'Inventory',
    'Reports',
    'Settings'
  ];

  stats = [
    { label: 'Products', value: '1,240', hint: '+8 added this week' },
    { label: 'Purchases', value: '42', hint: 'Awaiting 5 confirmations' },
    { label: 'Orders', value: '68', hint: '12 tickets pending' },
    { label: 'Inventory', value: '96%', hint: 'Healthy stock coverage' },
    { label: 'Reports', value: '7', hint: 'Downloads ready today' },
    { label: 'Manager', value: 'Signed in', hint: 'Full access' }
  ];

  featureTiles = [
    {
      title: 'Product management',
      description: 'Add, edit, and categorize SKUs with pricing and tax rules.'
    },
    {
      title: 'Purchases',
      description: 'Capture supplier deliveries and match against POs.'
    },
    {
      title: 'Orders & ticket printing',
      description: 'Process sales and print customer tickets on demand.'
    },
    {
      title: 'Inventory view',
      description: 'Track on-hand, reserved, and low-stock alerts in one view.'
    },
    {
      title: 'Reports & downloads',
      description: 'Export daily summaries, stock sheets, and sales insights.'
    },
    {
      title: 'Settings (future)',
      description: 'Configure roles, locations, and automation rules.'
    }
  ];

  ngOnInit(): void {
    this.timer = setInterval(() => {
      this.currentDateTime = new Date();
    }, 1000);
  }

  ngOnDestroy(): void {
    if (this.timer) {
      clearInterval(this.timer);
    }
  }

  get formattedDate(): string {
    return this.currentDateTime.toLocaleDateString('en-US', {
      weekday: 'long',
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    });
  }

  get formattedTime(): string {
    return this.currentDateTime.toLocaleTimeString('en-US', {
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    });
  }
}
