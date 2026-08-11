import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError, throwError } from 'rxjs';

import { environment } from '../../../environments/environment';

import {
  LoginRequest,
  RegisterRequest,
  AuthResponse,
  UserResponse
} from '../models/auth.model';

import { mapApiError } from '../errors/api-error.mapper';


@Injectable({
  providedIn: 'root'
})
export class AuthApiService {


  private readonly backendBaseUrl =
    environment.backendBaseUrl;


  constructor(
    private http: HttpClient
  ) {}


  register(
    payload: RegisterRequest
  ): Observable<UserResponse> {

    return this.http.post<UserResponse>(

      `${this.backendBaseUrl}api/auth/register`,

      payload

    ).pipe(

      catchError(error =>
        throwError(() => mapApiError(error))
      )

    );

  }


  login(
    payload: LoginRequest
  ): Observable<AuthResponse> {

    return this.http.post<AuthResponse>(

      `${this.backendBaseUrl}api/auth/login`,

      payload

    ).pipe(

      catchError(error =>
        throwError(() => mapApiError(error))
      )

    );

  }

}