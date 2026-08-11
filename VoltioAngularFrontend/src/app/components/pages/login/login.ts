import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/auth/auth.service';

@Component({
  selector: 'app-login',
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login {
  private fb = inject(NonNullableFormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);

  // State variables
  error = signal<string | null>(null);
  isPending = signal<boolean>(false);

  // Checks registration
  isRegistered = signal<boolean>(this.router.currentNavigation()?.extras.state?.['registered'] ?? false);

  // Form input definition
  loginForm = this.fb.group({
    username: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required]]
  });

  handleSubmit(): void {
    if (this.loginForm.invalid) {
      return;
    }

    this.isPending.set(true);
    this.error.set(null);

    const { username, password } = this.loginForm.getRawValue();

    this.authService.login({ username, password }).subscribe({
      next: () => {
        this.isPending.set(false);
        // Redirect user to the appropriate home page
        this.router.navigate(['/home']);
      },
      error: (err) => {
        this.isPending.set(false);
        this.error.set(err.message || 'An error occured during authentication.');
      }
    });
  }
}
