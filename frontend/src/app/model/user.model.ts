// export interface User {
//   id?: number;
//   username: string;
//   password?: string;
//   role: 'SUPER_MANAGER' | 'BRANCH_MANAGER';
//   branchId?: number;
//   active: boolean;
// }
export interface User {
  id: number;
  fullName: string;
  username: string;
  email?: string;   // optional, since some users may not have email
  phone?: string;   // optional
  role: string;
  branchId: number;
}
