import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from './auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  const authHeader = auth.getAuthHeader();

  const handled = authHeader
    ? next(req.clone({ setHeaders: { Authorization: authHeader } }))
    : next(req);

  return handled.pipe(
    catchError((error: HttpErrorResponse) => {
      // A persisted credential can go stale (e.g. the password was rotated). A
      // 401 means it's no longer valid, so clear it and send the user back to
      // login rather than leaving them on a page where every request fails.
      if (error.status === 401) {
        auth.logout();
        router.navigate(['/login']);
      }
      return throwError(() => error);
    }),
  );
};
