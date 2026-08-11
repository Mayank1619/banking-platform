import { Component, OnInit, signal } from '@angular/core';
import { Customer } from '../../core/models/customer';
import { MOCK_CUSTOMERS } from '../../mock-data/customer_for_admin';

@Component({
  selector: 'app-customer-profile',
  imports: [],
  templateUrl: './customer-profile.html',
  styleUrl: './customer-profile.css',
})
export class CustomerProfile implements OnInit {
  isAuthenticated = true; //This may not be needed and I should return to it (serves to protect this route)
  isAdmin = true; //Boolean for if admin and will have to be changed to actually check 
  isEditing = signal<boolean>(false);

  startEdit() {
    this.isEditing.update(currentValue => !currentValue);
  }

  cancelEdit() {
    this.isEditing.update(currentValue => !currentValue);
  }

  submitEdit() {
    // Function for setting the new user data and ensuring it gets saved
  }
  
  customers: Customer[] = [];
  
  // When we implement some sort of service to check authentication
  ngOnInit(): void {
    this.customers = MOCK_CUSTOMERS;
    // this.isAdmin = this.authService.hasRole('admin');
  }
}
