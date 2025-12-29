import { Component } from '@angular/core';
import { User } from '../../model/user.model';
import e from 'cors';



@Component({
  selector: 'app-users',
  standalone: true,
  templateUrl: './user.component.html'
})
export class UserComponent {

  users: User[] = [
    { id: 1, username: 'manager1', role: 'BRANCH_MANAGER', branchId: 1, active: true },
    { id: 2, username: 'manager2', role: 'BRANCH_MANAGER', branchId: 2, active: false }
  ];

  toggleStatus(user: User) {
    user.active = !user.active;
  }
}


export default UserComponent;
