import { Injectable } from '@angular/core';
import { Observable, tap } from 'rxjs';

import {
  LoginRequest,
  RegisterRequest,
  AuthResponse,
  UserResponse,
  AuthState
} from '../models/auth.model';

import { AuthApiService } from '../services/auth-api.service';

import { AuthStorageService } from './auth-storage.service';
import { JwtService } from './jwt.service';


@Injectable({
  providedIn: 'root'
})
export class AuthService {


  constructor(

    private authApi: AuthApiService,

    private authStorage: AuthStorageService,

    private jwtService: JwtService

  ) { }



  login(
    credentials: LoginRequest
  ): Observable<AuthResponse> {


    return this.authApi.login(credentials)

      .pipe(

        tap(response => {
          const expiresAt =
            Date.now() +

            (
              response.expiresIn * 1000
            );

          this.authStorage.write({

            accessToken: response.accessToken,

            refreshToken: response.refreshToken,

            expiresAt

          });


        })

      );


  }


  register(
    payload: RegisterRequest
  ): Observable<UserResponse> {


    return this.authApi.register(payload);

  }


  logout(): void {

    this.authStorage.clear();

  }


  isAuthenticated(): boolean {

    return this.authStorage.hasFreshToken();

  }


  getAuthState(): AuthState {

    return this.authStorage.read();

  }


  getAccessToken(): string | null {

    return this.authStorage.getAccessToken();

  }

  getRoles(): string[] {

    return this.jwtService.getRoles();
  
  }


}