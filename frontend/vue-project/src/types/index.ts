// ==================== API 通用响应 ====================
export interface Result<T = unknown> {
  code: number
  message: string
  data: T
  timestamp: number
  requestId: string
}

// ==================== 枚举 ====================
export type AuditState = 'pending' | 'approved' | 'rejected'

export type HardwareType =
  | 'CPU'
  | 'MOTHERBOARD'
  | 'GRAPHICS_CARD'

export type AdminLevel = 'SUPER' | 'NORMAL'

export type UserRole = 'ADMIN' | 'ORDINARY'

// ==================== 认证相关 ====================
export interface LoginRequest {
  username: string
  password: string
}

export interface LoginResponse {
  token: string
  username: string
  userId: string
  role: string
}

export interface RegisterRequest {
  userName: string
  password: string
  userMailbox?: string
  userPhone?: string
}

// ==================== 用户实体 ====================
export interface User {
  id?: number
  username: string
  password?: string
  email?: string
  userId: string
  role: string
  createTime?: string
}

export interface OrdinaryUser {
  userId: string
  userName: string
  userPhone?: string
  userMailbox?: string
  registerTime?: string
  user?: User
}

export interface Administrator {
  userId: string
  adminDept?: string
  adminLevel: AdminLevel
  createTime?: string
  user?: User
}

// ==================== 用户 DTO / VO ====================
export interface UserDTO {
  userId: string
  userPassword: string
  userType: string
  userName: string
  userPhone?: string
  userMailbox?: string
  adminDept?: string
  adminLevel?: string
}

export interface UserQueryDTO {
  keyword?: string
  startTime?: string
  endTime?: string
  userType?: string
  sortField?: string
  sortOrder?: string
  pageNum?: number
  pageSize?: number
}

export interface UserVO {
  userId: string
  userName: string
  userPhone?: string
  userMailbox?: string
  userType: string
  registerTime: string
  collectCount: number
  evaluationCount: number
  commentCount: number
}

export interface UserDetailVO extends UserVO {
  lastLoginTime?: string
  avatar?: string
}

// ==================== 硬件相关实体 ====================
export interface HardwareInf {
  hardwareId: number
  userId?: string
  hardwareName: string
  hardwareType: string
  hardwareBrand: string
  hardwareModel: string
  hardwarePrice: number
  auditState: string
  publishTime?: string
  auditTime?: string
  updateTime?: string
  auditStateName?: string
  hardwareTypeName?: string
  cpuInf?: CpuInf
  motherboardInf?: MotherboardInf
  graphicsCardInf?: GraphicsCardInf
}

export interface CpuInf {
  cpuId: number
  hardwareId: number
  coreCount: number
  threadCount: number
  baseFreq: number
  interfaceType: string
  tdpPower: number
  supportMemType: string
  hardwareInf?: HardwareInf
}

export interface MotherboardInf {
  mbdId: number
  hardwareId: number
  cpuInterface: string
  mbForm: string
  memSlotCount: number
  supportMemType: string
  m2SlotCount: number
  hardwareInf?: HardwareInf
}

export interface GraphicsCardInf {
  gcId: number
  hardwareId: number
  coreModel: string
  vramCap: string
  vramType: string
  powerConsump: number
  gcLength: number
  hardwareInf?: HardwareInf
}

// ==================== 硬件查询 DTO / VO ====================
export interface HardwareQueryDTO {
  hardwareName?: string
  hardwareTypes?: string[]
  hardwareBrands?: string[]
  minPrice?: number
  maxPrice?: number
  auditState?: string
  sortField?: string
  sortOrder?: string
  pageNum?: number
  pageSize?: number
  cpuInterface?: string
  supportMemType?: string
  mbForm?: string
  minCoreCount?: number
  maxCoreCount?: number
  coreModel?: string
  minPower?: number
  maxPower?: number
}

export interface HardwareVO {
  hardwareId: number
  hardwareName: string
  hardwareType: string
  hardwareBrand: string
  hardwareModel: string
  hardwarePrice: number
  auditState: string
  publishTime: string
  details: Record<string, unknown>
  collectCount: number
  evaluationCount: number
  isCollected: boolean
  averageRating: number
  recommendationScore: number
  recommendationReason: string
}

// ==================== 评测相关 ====================
export interface Evaluation {
  evaluationId: number
  userId: string
  hardwareId: number
  evaluationTitle: string
  auditState: string
  publishTime?: string
  auditTime?: string
  perfTestData: string
  usageExperience: string
  prosAndCons?: string
  user?: User
  hardwareInf?: HardwareInf
  comments?: Comment[]
  images?: MediaInf[]
  commentCount?: number
  isCollected?: boolean
}

export interface EvaluationDTO {
  hardwareId: number
  evaluationTitle: string
  perfTestData: string
  usageExperience: string
  prosAndCons?: string
  imageUrls?: string[]
}

// ==================== 评论相关 ====================
export interface Comment {
  commentId: string
  evaluationId: number
  userId: string
  content: string
  auditState: string
  publishTime?: string
  auditTime?: string
  user?: User
  evaluation?: Evaluation
  images?: MediaInf[]
}

// ==================== 收藏相关 ====================
export interface Collect {
  collectId: string
  hardwareId: number
  userId: string
  collectTime?: string
  user?: User
  hardwareInf?: HardwareInf
}

// ==================== 媒体相关 ====================
export interface MediaInf {
  mediaId: number
  evaluationId?: number
  commentId?: string
  mediaUrl: string
  uploadTime?: string
  evaluation?: Evaluation
  comment?: Comment
}

// ==================== 推荐相关 ====================
export interface RecommendationRequest {
  budget: number
  purpose: 'gaming' | 'design' | 'office' | 'development'
  performanceLevel: 'entry' | 'mid' | 'high' | 'enthusiast'
  includePeripherals?: boolean
  preferredBrand?: string
  needRGB?: number
  sizePreference?: 'mini' | 'compact' | 'standard' | 'full'
}

export interface RecommendationVO {
  totalPrice: number
  originalPrice: number
  discount: number
  purpose: string
  performanceLevel: string
  compatibilityScore: number
  cpu?: HardwareVO
  motherboard?: HardwareVO
  graphicsCard?: HardwareVO
  memory?: HardwareVO
  storage?: HardwareVO
  powerSupply?: HardwareVO
  state?: HardwareVO
  peripherals: HardwareVO[]
  strengths: string
  weaknesses: string
  compatibilityIssues: string[]
  priceDistribution: Record<string, number>
  upgradeSuggestions: string[]
}

// ==================== 分页 ====================
export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}
