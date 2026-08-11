import { Component, OnInit } from '@angular/core';
import { Customer } from '../../core/models/customer';
import { MOCK_CUSTOMERS } from '../../mock-data/customer_for_admin';
import { Button } from '../../components/shared-components/button/button';

@Component({
  selector: 'app-customer-detail',
  imports: [Button],
  templateUrl: './customer-detail.html',
  styleUrl: './customer-detail.css',
})
export class CustomerDetail implements OnInit {
  isAuthenticated = true; //This may not be needed and I should return to it (serves to protect this route)
  isAdmin = true; //Boolean for if admin and will have to be changed to actually check 
  
  customers: Customer[] = [];
  
  // When we implement some sort of service to check authentication
  ngOnInit(): void {
    this.customers = MOCK_CUSTOMERS;
    // this.isAdmin = this.authService.hasRole('admin');
  }

}


