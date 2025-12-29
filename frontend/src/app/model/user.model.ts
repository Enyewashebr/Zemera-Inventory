export interface User {
  id?: number;
  username: string;
  password?: string;
  role: 'SUPER_MANAGER' | 'BRANCH_MANAGER';
  branchId?: number;
  active: boolean;
}
