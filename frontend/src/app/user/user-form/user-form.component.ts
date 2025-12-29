import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { User } from '../../model/user.model';

@Component({
  selector: 'app-user-form',
  templateUrl: './user-form.component.html'
})
export class UserFormComponent {

  user: User = {
    username: '',
    password: '',
    role: 'BRANCH_MANAGER',
    branchId: 1,
    active: true
  };

  constructor(private router: Router) {}

  save() {
    console.log('Created user:', this.user);
    this.router.navigate(['/users']);
  }
}
