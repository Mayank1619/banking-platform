import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../auth/auth.service';

export const homeRedirectGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.getRoles().includes('ADMIN')) {
    return router.createUrlTree(['/home/all-accounts'])
  }

  return router.createUrlTree(['/home/dashboard']);
};
