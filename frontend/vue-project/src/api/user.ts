import request from './request'
import type { Result, User } from '@/types'

export function getUserById(id: number): Promise<Result<User>> {
  return request.get(`/user/${id}`).then((res) => res.data)
}

export function addUser(data: User): Promise<Result<boolean>> {
  return request.post('/user', data).then((res) => res.data)
}

export function listUsers(): Promise<Result<User[]>> {
  return request.get('/user/list').then((res) => res.data)
}

export function userTest(): Promise<Result<string>> {
  return request.get('/user/test').then((res) => res.data)
}

export interface CollectedItem {
  collectId: string
  collectTime: string
  hardwareId: number
  hardwareName: string
  hardwareType: string
  hardwareBrand: string
  hardwareModel: string
  hardwarePrice: number
  imageUrl: string | null
}

export interface EvaluationItem {
  evaluationId: number
  evaluationTitle: string
  auditState: string
  publishTime: string
  perfTestData: string
  usageExperience: string
  prosAndCons: string | null
  images: string[]
  hardware: {
    hardwareId: number
    hardwareName: string
    hardwareType: string
    hardwareBrand: string
    hardwarePrice: number
    imageUrl: string | null
  } | null
}

export function getCollections(): Promise<CollectedItem[]> {
  return request.get('/user/profile/collections').then((res) => res.data.data)
}

export function getEvaluations(): Promise<EvaluationItem[]> {
  return request.get('/user/profile/evaluations').then((res) => res.data.data)
}

export function updateProfileUsername(username: string): Promise<{ username: string; message: string }> {
  return request.put('/user/profile/username', { username }).then((res) => res.data.data)
}

export function resetProfilePassword(newPassword: string): Promise<{ message: string }> {
  return request.post('/user/profile/password/reset', { newPassword }).then((res) => res.data.data)
}

export function deleteAccount(): Promise<{ message: string }> {
  return request.delete('/user/profile/account').then((res) => res.data.data)
}

export interface CollectResult {
  collected: boolean
  collectId?: string
  message: string
}

export interface CollectCheckResult {
  collected: boolean
  collectId?: string
}

export function toggleCollect(hardwareId: number): Promise<CollectResult> {
  return request.post('/user/profile/collect', { hardwareId }).then((res) => res.data.data)
}

export function checkCollected(hardwareId: number): Promise<CollectCheckResult> {
  return request.get(`/user/profile/collect/check/${hardwareId}`).then((res) => res.data.data)
}

export interface UploadResult {
  url: string
  originalName: string
}

export function uploadImage(file: File): Promise<UploadResult> {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/upload', formData).then((res) => res.data.data)
}

export interface CreateEvaluationParams {
  hardwareId: number
  evaluationTitle: string
  perfTestData: string
  usageExperience: string
  prosAndCons: string
  imageUrls: string[]
}

export interface CreateEvaluationResult {
  evaluationId: number
  message: string
}

export function createEvaluation(data: CreateEvaluationParams): Promise<CreateEvaluationResult> {
  return request.post('/user/profile/evaluate', data).then((res) => res.data.data)
}

export interface EvaluationDetail {
  evaluationId: number
  evaluationTitle: string
  auditState: string
  publishTime: string
  perfTestData: string
  usageExperience: string
  prosAndCons: string | null
  images: string[]
  publisherName: string
  comments: CommentItem[]
  hardware: {
    hardwareId: number
    hardwareName: string
    hardwareType: string
    hardwareBrand: string
    hardwarePrice: number
    imageUrl: string | null
  } | null
}

export interface CommentItem {
  commentId: string
  userId: string
  content: string
  publishTime: string
  userName: string
  images: string[]
}

export function createComment(evaluationId: number, data: { content: string; imageUrls: string[] }): Promise<{ commentId: string; message: string }> {
  return request.post(`/user/profile/evaluation/${evaluationId}/comment`, data).then((res) => res.data.data)
}

export function deleteEvaluation(id: number): Promise<{ message: string }> {
  return request.delete(`/user/profile/evaluation/${id}`).then((res) => res.data.data)
}

export function deleteComment(id: string): Promise<{ message: string }> {
  return request.delete(`/user/profile/comment/${id}`).then((res) => res.data.data)
}

export function getEvaluationById(id: number): Promise<EvaluationDetail> {
  return request.get(`/user/profile/evaluation/${id}`).then((res) => res.data.data)
}

export interface AllEvaluationItem {
  evaluationId: number
  evaluationTitle: string
  publishTime: string
  usageExperience: string
  publisherName: string
  images: string[]
  hardware: {
    hardwareId: number
    hardwareName: string
    hardwareType: string
    hardwareBrand: string
    hardwarePrice: number
    imageUrl: string | null
  } | null
}

export function getAllEvaluations(): Promise<AllEvaluationItem[]> {
  return request.get('/evaluations').then((res) => res.data.data)
}

// ==================== 推荐算法 ====================

export interface RecommendInput {
  isGaming: boolean
  budget: number
  minCpuCores: number
  preferredGpuSeries?: string
  preferredMoboSize?: string
  preferredBrands?: string[]
}

export interface RecommendResultItem {
  totalPrice: number
  cpuId: number
  cpuName: string
  cpuBrand: string
  cpuCores: number
  cpuBaseFreq: number
  cpuPrice: number
  gpuId: number | null
  gpuName: string | null
  gpuBrand: string | null
  gpuSeries: string | null
  gpuVram: string | null
  gpuPrice: number | null
  mbId: number
  mbName: string
  mbBrand: string
  mbForm: string
  mbCpuInterface: string
  mbPrice: number
  score: number
  hitCount: number
  totalSoft: number
  hitDetails: string[]
  missDetails: string[]
}

export interface RecommendResult {
  passed: boolean
  totalCandidates: number
  suggestion: string | null
  imageMap: Record<number, string>
  results: RecommendResultItem[]
}

export function getRecommendations(data: RecommendInput): Promise<RecommendResult> {
  return request.post('/recommend', data).then((res) => res.data.data)
}

// ==================== 推荐榜 ====================

export interface RankItem {
  hardwareId: number
  hardwareName: string
  hardwareBrand: string
  hardwarePrice: number
  imageUrl: string | null
  score: number
  // CPU
  coreCount?: number
  threadCount?: number
  baseFreq?: number
  interfaceType?: string
  tdpPower?: number
  // GPU
  coreModel?: string
  vramCap?: string
  vramType?: string
  powerConsump?: number
  // MB
  cpuInterface?: string
  mbForm?: string
  memSlotCount?: number
  m2SlotCount?: number
}

export interface RankingData {
  cpuRanking: RankItem[]
  gpuRanking: RankItem[]
  mbRanking: RankItem[]
}

export function getRanking(): Promise<RankingData> {
  return request.get('/ranking').then((res) => res.data.data)
}

// ==================== 管理员 ====================

export interface AdminHardwareItem {
  hardwareId: number
  hardwareName: string
  hardwareType: string
  hardwareBrand: string
  hardwareModel: string
  hardwarePrice: number
  auditState: string
  publishTime: string
  imageUrl: string | null
  // CPU
  coreCount?: number
  threadCount?: number
  baseFreq?: number
  interfaceType?: string
  tdpPower?: number
  supportMemType?: string
  // GPU
  coreModel?: string
  vramCap?: string
  vramType?: string
  powerConsump?: number
  gcLength?: number
  // MB
  cpuInterface?: string
  mbForm?: string
  memSlotCount?: number
  mbSupportMemType?: string
  m2SlotCount?: number
}

export interface AdminEvalItem {
  evaluationId: number
  evaluationTitle: string
  auditState: string
  publishTime: string
  perfTestData: string
  usageExperience: string
  prosAndCons: string | null
  hardwareId: number
  hardwareName?: string
  hardwareType?: string
  publisherName: string
  images: string[]
}

export interface AdminStats {
  hardwareCount: number
  cpuCount: number
  gpuCount: number
  mbCount: number
  evalCount: number
  pendingEvalCount: number
  commentCount: number
  pendingCommentCount: number
  userCount: number
}

export interface AdminCommentItem {
  commentId: string
  evaluationId: number
  evaluationTitle: string
  content: string
  auditState: string
  publishTime: string
  userName: string
  images: string[]
}

export function adminGetHardware(): Promise<AdminHardwareItem[]> {
  return request.get('/admin/hardware').then((res) => res.data.data)
}
export function adminCreateHardware(data: Record<string, unknown>): Promise<{ hardwareId: number; message: string }> {
  return request.post('/admin/hardware', data).then((res) => res.data.data)
}
export function adminUpdateHardware(id: number, data: Record<string, unknown>): Promise<{ message: string }> {
  return request.put(`/admin/hardware/${id}`, data).then((res) => res.data.data)
}
export function adminDeleteHardware(id: number): Promise<{ message: string }> {
  return request.delete(`/admin/hardware/${id}`).then((res) => res.data.data)
}
export function adminGetEvaluations(state: string): Promise<AdminEvalItem[]> {
  return request.get('/admin/evaluations', { params: { state } }).then((res) => res.data.data)
}
export function adminAuditEvaluation(id: number, action: string): Promise<{ message: string }> {
  return request.put(`/admin/evaluation/${id}/audit`, { action }).then((res) => res.data.data)
}
export function adminGetComments(state: string): Promise<AdminCommentItem[]> {
  return request.get('/admin/comments', { params: { state } }).then((res) => res.data.data)
}
export function adminAuditComment(id: string, action: string): Promise<{ message: string }> {
  return request.put(`/admin/comment/${id}/audit`, { action }).then((res) => res.data.data)
}
export function adminDeleteEvaluation(id: number): Promise<{ message: string }> {
  return request.delete(`/admin/evaluation/${id}`).then((res) => res.data.data)
}
export function adminDeleteComment(id: string): Promise<{ message: string }> {
  return request.delete(`/admin/comment/${id}`).then((res) => res.data.data)
}
export function adminGetStats(): Promise<AdminStats> {
  return request.get('/admin/stats').then((res) => res.data.data)
}
