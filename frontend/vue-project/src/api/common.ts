import request from './request'
import type { Result } from '@/types'

function unwrap<T>(res: { data: Result<T> }): T {
  const result = res.data
  if (result && typeof result === 'object' && 'data' in result) {
    return result.data as T
  }
  return res.data as unknown as T
}

export function hello(): Promise<string> {
  return request.get('/hello').then(unwrap)
}

export function getHardware(): Promise<string> {
  return request.get('/hardware').then(unwrap)
}

export function testDb(): Promise<string> {
  return request.get('/test/db').then(unwrap)
}

export function testTables(): Promise<string[]> {
  return request.get('/test/tables').then(unwrap)
}

export function testUsers(): Promise<Record<string, unknown>[]> {
  return request.get('/test/users').then(unwrap)
}

export function testHardware(): Promise<Record<string, unknown>[]> {
  return request.get('/test/hardware').then(unwrap)
}
