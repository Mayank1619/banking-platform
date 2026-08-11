import { Customer, type, role } from '../core/models/customer';

export const MOCK_CUSTOMERS: Customer[] = [
  {
    id: 101,
    name: 'Alice Vance',
    email: 'alice.vance@example.com',
    address: '123 Maple Street, Austin, TX 78701',
    type: type.Person,
    role: role.Customer,
    dob: '01.01.2001'
  },
  {
    id: 102,
    name: 'Apex Global Logistics',
    email: 'billing@apexglobal.com',
    address: '888 Industrial Parkway, Suite 400, Chicago, IL 60601',
    type: type.Company,
    role: role.Customer,
    dob: '03.01.2001'
  },
  {
    id: 103,
    name: 'Marcus Brody',
    email: 'm.brody@museum-ops.org',
    address: '742 Evergreen Terrace, Seattle, WA 98101',
    type: type.Person,
    role: role.Customer,
    dob: '04.01.2001'
  },
  {
    id: 104,
    name: 'Zenith Tech Solutions',
    email: 'admin@zenithtech.io',
    address: '500 Innovation Way, San Jose, CA 95110',
    type: type.Company,
    role: role.Customer,
    dob: '05.01.2001'
  }
];