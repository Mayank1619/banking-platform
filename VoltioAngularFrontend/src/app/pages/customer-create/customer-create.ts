import { Component, OnInit, signal } from '@angular/core';
import { Customer, type } from '../../core/models/customer';
import { MOCK_CUSTOMERS } from '../../mock-data/customer_for_admin';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-customer-create',
  imports: [FormsModule],
  templateUrl: './customer-create.html',
  styleUrl: './customer-create.css',
})
  export class CustomerCreate {
    isAuthenticated = true; //This may not be needed and I should return to it (serves to protect this route)
    isAdmin = true; //Boolean for if admin and will have to be changed to actually check 
    success = signal<boolean>(false);
    error = signal<boolean>(false);

    selectedType:String = 'Person'; //By default
    types = Object.values(type);

    customers: Customer[] = [];

    handleSubmit() {

    }

    ngOnInit(): void {
      this.customers = MOCK_CUSTOMERS;
      // this.isAdmin = this.authService.hasRole('admin');
    }


  }
