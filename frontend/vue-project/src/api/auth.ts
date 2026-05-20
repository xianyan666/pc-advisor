import request from './request'
import type { LoginRequest, LoginResponse, RegisterRequest, Result } from '@/types'

export function login(data: LoginRequest): Promise<Result<LoginResponse>> {
  return request.post('/auth/login', data).then((res) => res.data)
}

export function register(data: RegisterRequest): Promise<Result<string>> {
  return request.post('/auth/register', data).then((res) => res.data)
}

export function logout(): Promise<Result<string>> {
  return request.post('/auth/logout').then((res) => res.data)
}

export function checkUsername(username: string): Promise<Result<boolean>> {
  return request.get('/auth/check-username', { params: { username } }).then((res) => res.data)
}

export function checkEmail(email: string): Promise<Result<boolean>> {
  return request.get('/auth/check-email', { params: { email } }).then((res) => res.data)
}
