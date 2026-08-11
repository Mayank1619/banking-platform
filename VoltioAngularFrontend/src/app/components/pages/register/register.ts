import { Component, inject, signal, computed } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../../core/auth/auth.service';

// Component configuration constants
const CUSTOMER_TYPES = ['PERSONAL', 'BUSINESS'] as const;
const REGISTER_CUSTOMER_TYPE_LABELS: Record<string, string> = {
  PERSONAL: 'Personal',
  BUSINESS: 'Business',
};

@Component({
  selector: 'app-register',
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './register.html',
  styleUrl: './register.css'
})
export class Register {

  private fb = inject(NonNullableFormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);

  // Constants
  readonly customerTypes = CUSTOMER_TYPES;
  readonly labels = REGISTER_CUSTOMER_TYPE_LABELS;

  step = signal<'selectType' | 'details'>('selectType');
  activeStep = computed(() => this.step() === 'selectType' ? 1 : 2);

  error = signal<string | null>(null);
  isPending = signal<boolean>(false);

  // Form definition
  registerForm = this.fb.group({
    type: ['PERSONAL', [Validators.required]],
    username: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8)]],
    name: ['', [Validators.required]],
    address: ['', [Validators.required]],
    dateOfBirth: [''],
    governmentBusinessNumber: ['', [Validators.pattern('[0-9]{9}')]]
  });


  // Binds the choice to a signal
  private selectedType = toSignal(this.registerForm.get('type')!.valueChanges, { initialValue: this.registerForm.get('type')!.value });

  // Checks the type of client 
  isPerson = computed(() => this.selectedType() === 'PERSONAL');

  // This does not work because it is not reading a signal and thus never gets updated again
  // isPerson = computed(() => {
  //   const selectedType = this.registerForm.get('type')?.value;
  //   return selectedType === 'PERSONAL';
  // });

  setStep(targetStep: 'selectType' | 'details'): void {
    this.step.set(targetStep);
  }


  handleSubmit(): void {
    if (this.registerForm.invalid) {
      return;
    }

    this.isPending.set(true);
    this.error.set(null);

    const payload = this.registerForm.getRawValue();

    this.authService.register(payload).subscribe({
      next: () => {
        this.isPending.set(false);
        // Pass complete registration state to the login page route
        this.router.navigate(['/login'], { state: { registered: true } });
      },
      error: (err) => {
        this.isPending.set(false);
        this.error.set(err.message || 'An error occurred during registration.');
      }
    });
  }
}
