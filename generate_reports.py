# -*- coding: utf-8 -*-
"""Generate 4 project reports as .docx files"""

from docx import Document
from docx.shared import Pt, Cm
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.oxml.ns import qn
import os

OUTPUT_DIR = "E:/data/Desktop/IdeaProjects/项目报告"
PROJECT_NAME = "电脑硬件分析推荐系统"
TEAM = "李锦轩、李俊泽、黄国政"
ADVISOR = "陈海霞"
SCHOOL = "南京航空航天大学 计算机科学与技术学院"
DATE = "2026-05-20"
VERSION = "1.0"

def set_cell_font(cell, text, bold=False, size=10):
    cell.text = ""
    p = cell.paragraphs[0]
    p.alignment = WD_ALIGN_PARAGRAPH.LEFT
    run = p.add_run(text)
    run.font.size = Pt(size)
    run.font.name = '宋体'
    run._element.rPr.rFonts.set(qn('w:eastAsia'), '宋体')
    run.bold = bold

def create_cover(doc, subtitle):
    for _ in range(4):
        doc.add_paragraph()
    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    run = p.add_run('{ ' + PROJECT_NAME + ' }')
    run.font.size = Pt(22)
    run.font.name = '黑体'
    run._element.rPr.rFonts.set(qn('w:eastAsia'), '黑体')
    run.bold = True
    doc.add_paragraph(); doc.add_paragraph()
    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    run = p.add_run(subtitle)
    run.font.size = Pt(26)
    run.font.name = '黑体'
    run._element.rPr.rFonts.set(qn('w:eastAsia'), '黑体')
    run.bold = True
    for _ in range(6):
        doc.add_paragraph()
    t = doc.add_table(rows=5, cols=2)
    t.style = 'Table Grid'
    for i, (label, value) in enumerate([
        ('文件标识', 'Company-Project-' + subtitle),
        ('当前版本', VERSION),
        ('作    者', TEAM),
        ('完成日期', DATE),
        ('指导老师', ADVISOR),
    ]):
        set_cell_font(t.cell(i, 0), label, bold=True, size=10)
        set_cell_font(t.cell(i, 1), value, size=10)
    doc.add_page_break()

def hdr(doc, text, level=1):
    h = doc.add_heading(text, level=level)
    for run in h.runs:
        run.font.name = '黑体'
        run._element.rPr.rFonts.set(qn('w:eastAsia'), '黑体')

def para(doc, text, bold=False, indent=False):
    p = doc.add_paragraph()
    if indent:
        p.paragraph_format.first_line_indent = Cm(0.74)
    run = p.add_run(text)
    run.font.size = Pt(11)
    run.font.name = '宋体'
    run._element.rPr.rFonts.set(qn('w:eastAsia'), '宋体')
    run.bold = bold

def make_table(doc, headers, rows):
    t = doc.add_table(rows=len(rows)+1, cols=len(headers))
    t.style = 'Table Grid'
    for i, h in enumerate(headers):
        set_cell_font(t.rows[0].cells[i], h, bold=True, size=9)
    for i, row in enumerate(rows):
        for j, val in enumerate(row):
            set_cell_font(t.rows[i+1].cells[j], val, size=9)
    return t

# ============================================================
# Report 1: 数据库设计报告
# ============================================================
def create_db_report():
    doc = Document()
    style = doc.styles['Normal']
    font = style.font; font.name = '宋体'; font.size = Pt(11)
    style.element.rPr.rFonts.set(qn('w:eastAsia'), '宋体')
    create_cover(doc, '数据库设计报告')

    hdr(doc, '0. 文档介绍')
    hdr(doc, '0.1 文档目的', 2)
    para(doc, '本文档旨在详细描述《电脑硬件分析推荐系统》的数据库设计，包括数据库环境、命名规范、逻辑设计、物理设计、安全性设计和优化策略。', indent=True)
    hdr(doc, '0.2 文档范围', 2)
    para(doc, '涵盖系统的全部数据库对象设计，包括用户体系（sys_user、OrdinaryUser、Administrator）、硬件信息体系（Hardware_Inf、CPU_Inf、GraphicsCard_Inf、Motherboard_Inf）、UGC体系（Evaluation、Comment、Collect、Media_Inf）以及关联关系的设计说明。', indent=True)
    hdr(doc, '0.3 读者对象', 2)
    para(doc, '系统开发人员、数据库管理员（DBA）、测试人员以及项目评审专家。', indent=True)
    hdr(doc, '0.4 参考文献', 2)
    para(doc, '[SPP-PROC-SD] SEPG，《系统设计规范》，南京航空航天大学，2025')
    para(doc, '[MySQL8] Oracle，《MySQL 8.0 Reference Manual》，Oracle Corporation，2023')
    hdr(doc, '0.5 术语与缩写解释', 2)
    make_table(doc, ['缩写/术语', '解释'], [
        ('SPP', '精简并行过程，Simplified Parallel Process'),
        ('SD', '系统设计，System Design'),
        ('ERD', '实体关系图，Entity-Relationship Diagram'),
        ('JWT', 'JSON Web Token，无状态身份认证'),
        ('UGC', '用户生成内容，User Generated Content'),
        ('ORM', '对象关系映射，Object-Relational Mapping'),
    ])

    hdr(doc, '1. 数据库环境说明')
    make_table(doc, ['项目', '说明'], [
        ('数据库系统', 'MySQL 8.0'),
        ('数据库名称', 'pcadvisor'),
        ('字符集编码', 'utf8mb4'),
        ('数据库地址', 'localhost:3306'),
        ('开发环境', 'Windows 11 + JDK 21 + Spring Boot 3.2.2'),
        ('ORM框架', 'MyBatis-Plus 3.5.5 + Spring Data JPA'),
    ])
    para(doc, '系统采用utf8mb4字符集以完整支持中文及Emoji字符。数据库与应用通过MyBatis-Plus进行ORM映射，所有表均使用InnoDB引擎以支持事务和外键约束。')

    hdr(doc, '2. 数据库的命名规则')
    para(doc, '本系统数据库遵循以下命名规范：')
    for r in [
        '数据库名称：小写英文，单词用下划线分隔（pcadvisor）。',
        '表名：采用PascalCase与下划线混合。核心表用"实体名_Inf"格式（Hardware_Inf、CPU_Inf），用户表用PascalCase（OrdinaryUser），关联表直接使用英文名词（Collect、Comment、Evaluation）。',
        '字段名：主表使用下划线分隔（hardware_name、audit_state），用户表使用camelCase（userName、userPhone）。',
        '主键：整型自增主键命名为id或表名+ID（HardwareID、userID）。',
        '索引：按idx_字段名格式命名（idx_hardware_type）。',
        '唯一约束：按uk_字段名格式命名（uk_username）。',
    ]:
        para(doc, '• ' + r)

    hdr(doc, '3. 逻辑设计')
    para(doc, '系统数据库逻辑设计采用实体-关系（E-R）模型，包含以下核心实体：', bold=True)
    for e in [
        '1. 系统用户（sys_user）：登录认证信息（用户名、加密密码、角色ADMIN/ORDINARY）。',
        '2. 普通用户（OrdinaryUser）：扩展信息（手机号、邮箱、注册时间），通过user_id关联sys_user。',
        '3. 管理员（Administrator）：管理信息（部门、级别SUPER/NORMAL），通过user_id关联sys_user。',
        '4. 硬件信息（Hardware_Inf）：核心数据（名称、类型CPU/MB/GPU、品牌、型号、价格、审核状态）。',
        '5. CPU信息（CPU_Inf）：专项参数（核心数、线程数、基础频率、接口类型、TDP功耗），通过hardware_id关联。',
        '6. 显卡信息（GraphicsCard_Inf）：专项参数（核心型号、显存容量/类型、功耗、长度），通过hardware_id关联。',
        '7. 主板信息（Motherboard_Inf）：专项参数（CPU接口、板型、内存/M.2插槽数、支持内存类型），通过hardware_id关联。',
        '8. 评测（Evaluation）：用户评测（标题、性能数据、使用体验、优缺点、审核状态），关联User和Hardware_Inf。',
        '9. 评论（Comment）：评测评论（内容、审核状态），关联User和Evaluation。',
        '10. 收藏（Collect）：硬件收藏记录，关联User和Hardware_Inf。',
        '11. 媒体资源（Media_Inf）：图片路径（media_url），通过hardware_id/evaluation_id/comment_id灵活关联。',
    ]:
        para(doc, e)
    para(doc, '')
    para(doc, '实体间关键关系：', bold=True)
    para(doc, '• sys_user ↔ OrdinaryUser/Administrator：通过user_id的一对一关系，区分用户身份。')
    para(doc, '• Hardware_Inf ↔ CPU_Inf/GraphicsCard_Inf/Motherboard_Inf：通过HardwareID的一对一关系，按硬件类型使用对应详情表。')
    para(doc, '• Evaluation ↔ Hardware_Inf：多对一关系（一个硬件可有多篇评测），推荐算法通过CPU接口类型与主板接口类型匹配生成兼容方案。')

    hdr(doc, '4. 物理设计')
    hdr(doc, '4.0 表汇总', 2)
    make_table(doc, ['表名', '中文名称', '说明'], [
        ('sys_user', '系统用户表', '登录认证（用户名、密码、角色）'),
        ('OrdinaryUser', '普通用户表', '用户扩展信息'),
        ('Administrator', '管理员表', '管理员信息'),
        ('Hardware_Inf', '硬件信息主表', '硬件核心数据'),
        ('CPU_Inf', 'CPU详情表', 'CPU专项参数'),
        ('GraphicsCard_Inf', '显卡详情表', '显卡专项参数'),
        ('Motherboard_Inf', '主板详情表', '主板专项参数'),
        ('Evaluation', '评测表', '用户评测内容'),
        ('Comment', '评论表', '评测评论'),
        ('Collect', '收藏表', '硬件收藏'),
        ('Media_Inf', '媒体资源表', '图片/多媒体URL'),
    ])

    # 表A: Hardware_Inf
    hdr(doc, '4.1 硬件信息主表 (Hardware_Inf)', 2)
    make_table(doc, ['字段名', '数据类型', '约束', '说明'], [
        ('HardwareID', 'INT', 'PRIMARY KEY, AUTO_INCREMENT', '硬件唯一标识'),
        ('hardware_name', 'VARCHAR(256)', 'NOT NULL', '硬件名称'),
        ('hardware_type', 'VARCHAR(32)', 'NOT NULL', '类型: CPU/MOTHERBOARD/GRAPHICS_CARD'),
        ('hardware_brand', 'VARCHAR(64)', '', '品牌'),
        ('hardware_model', 'VARCHAR(64)', '', '型号'),
        ('hardware_price', 'DECIMAL(10,2)', '', '价格'),
        ('audit_state', 'VARCHAR(20)', "DEFAULT 'pending'", '审核状态(pending/approved/rejected)'),
        ('publish_time', 'DATETIME', 'DEFAULT CURRENT_TIMESTAMP', '发布时间'),
        ('audit_time', 'DATETIME', '', '审核时间'),
        ('update_time', 'DATETIME', '', '更新时间'),
    ])
    para(doc, '索引：idx_hardware_type (hardware_type), idx_hardware_brand (hardware_brand)')

    # 表B: CPU_Inf
    hdr(doc, '4.2 CPU详情表 (CPU_Inf)', 2)
    make_table(doc, ['字段名', '数据类型', '约束', '说明'], [
        ('cpuId', 'INT', 'PRIMARY KEY, AUTO_INCREMENT', 'CPU记录ID'),
        ('hardware_id', 'INT', 'NOT NULL', '关联Hardware_Inf.HardwareID'),
        ('core_count', 'INT', '', '核心数'),
        ('thread_count', 'INT', '', '线程数'),
        ('base_freq', 'DECIMAL(5,2)', '', '基础频率(GHz)'),
        ('interface_type', 'VARCHAR(32)', '', 'CPU接口类型(如LGA1700/AM5)'),
        ('tdp_power', 'INT', '', 'TDP功耗(W)'),
        ('support_mem_type', 'VARCHAR(32)', '', '支持内存类型(如DDR5)'),
    ])

    # 表C: GraphicsCard_Inf
    hdr(doc, '4.3 显卡详情表 (GraphicsCard_Inf)', 2)
    make_table(doc, ['字段名', '数据类型', '约束', '说明'], [
        ('gcId', 'INT', 'PRIMARY KEY, AUTO_INCREMENT', '显卡记录ID'),
        ('hardware_id', 'INT', 'NOT NULL', '关联Hardware_Inf.HardwareID'),
        ('core_model', 'VARCHAR(128)', '', '核心型号(如AD102)'),
        ('vram_cap', 'VARCHAR(16)', '', '显存容量(如12GB)'),
        ('vram_type', 'VARCHAR(16)', '', '显存类型(如GDDR6X)'),
        ('power_consump', 'INT', '', '功耗(W)'),
        ('gc_length', 'INT', '', '显卡长度(mm)'),
    ])

    # 表D: Motherboard_Inf
    hdr(doc, '4.4 主板详情表 (Motherboard_Inf)', 2)
    make_table(doc, ['字段名', '数据类型', '约束', '说明'], [
        ('mbdId', 'INT', 'PRIMARY KEY, AUTO_INCREMENT', '主板记录ID'),
        ('hardware_id', 'INT', 'NOT NULL', '关联Hardware_Inf.HardwareID'),
        ('cpu_interface', 'VARCHAR(32)', '', 'CPU接口类型(LGA1700/AM5等)'),
        ('mb_form', 'VARCHAR(16)', '', '板型(ATX/M-ATX/ITX)'),
        ('mem_slot_count', 'INT', '', '内存插槽数'),
        ('support_mem_type', 'VARCHAR(32)', '', '支持内存类型'),
        ('m2_slot_count', 'INT', '', 'M.2插槽数'),
    ])

    # 表E: sys_user
    hdr(doc, '4.5 系统用户表 (sys_user)', 2)
    make_table(doc, ['字段名', '数据类型', '约束', '说明'], [
        ('id', 'BIGINT', 'PRIMARY KEY, AUTO_INCREMENT', '主键ID'),
        ('user_id', 'VARCHAR(32)', 'UNIQUE', '业务用户ID'),
        ('username', 'VARCHAR(64)', 'UNIQUE, NOT NULL', '用户名'),
        ('password', 'VARCHAR(128)', 'NOT NULL', '密码(BCrypt加密)'),
        ('email', 'VARCHAR(128)', '', '邮箱'),
        ('role', 'VARCHAR(20)', "DEFAULT 'ORDINARY'", '角色: ADMIN/ORDINARY'),
        ('create_time', 'DATETIME', 'DEFAULT CURRENT_TIMESTAMP', '创建时间'),
    ])

    hdr(doc, '5. 安全性设计')
    hdr(doc, '5.1 防止用户直接操作数据库的方法', 2)
    para(doc, '（1）应用层隔离：所有数据库操作通过Spring Boot后端API进行，不直接暴露数据库端口。前端与数据库之间通过RESTful API + JWT认证隔离。')
    para(doc, '（2）参数化查询：使用MyBatis-Plus的BaseMapper进行数据库操作，所有SQL参数自动绑定，从根本上防止SQL注入攻击。')
    para(doc, '（3）认证拦截：Spring Security + JwtAuthenticationFilter对所有非公开API进行Token校验。')
    hdr(doc, '5.2 用户帐号密码的加密方法', 2)
    para(doc, '系统采用BCryptPasswordEncoder对用户密码进行哈希加密存储。BCrypt算法内置盐值（salt），每次加密产生不同密文，有效抵御彩虹表攻击。登录时通过passwordEncoder.matches()进行密文比对。')
    hdr(doc, '5.3 角色与权限', 2)
    para(doc, '• ADMIN（管理员）：硬件管理、评测审核、评论审核、数据统计。通过@PreAuthorize("hasRole(\'ADMIN\')")控制。')
    para(doc, '• ORDINARY（普通用户）：浏览硬件、查看排行、获取推荐、发布评测/评论、收藏。')
    para(doc, '• 公开接口：硬件浏览、排行查看、推荐算法、评测列表无需认证即可访问。')

    hdr(doc, '6. 优化')
    para(doc, '（1）索引优化：在hardware_type、hardware_brand、audit_state等高频查询字段建立B+树索引。用户名和用户ID设置唯一索引。')
    para(doc, '（2）读取优化：MyBatis-Plus配置map-underscore-to-camel-case=true，自动进行命名转换。')
    para(doc, '（3）表结构优化：CPU/显卡/主板详情拆分到独立子表，避免宽表问题，支持按类型的灵活扩展。')
    para(doc, '（4）连接池：使用HikariCP连接池（Spring Boot默认），提供高效数据库连接管理。')

    hdr(doc, '7. 数据库管理与维护说明')
    para(doc, '（1）数据库初始化：通过init.sql脚本完成建表和种子数据插入，脚本采用幂等性设计（先DROP后CREATE），可重复执行。')
    para(doc, '（2）数据备份：建议使用mysqldump工具定期备份pcadvisor数据库，备份周期为每日增量+每周全量。')
    para(doc, '（3）版本迁移：数据库结构变更通过更新init.sql脚本管理。')
    para(doc, '（4）监控日志：MyBatis-Plus配置log-impl=StdOutImpl在开发环境打印SQL语句，生产环境建议关闭。')

    path = os.path.join(OUTPUT_DIR, '001 A 电脑硬件分析推荐系统 数据库设计报告.docx')
    doc.save(path)
    print('Created: 数据库设计报告')

# ============================================================
# Report 2: 系统测试计划
# ============================================================
def create_test_plan():
    doc = Document()
    style = doc.styles['Normal']
    font = style.font; font.name = '宋体'; font.size = Pt(11)
    style.element.rPr.rFonts.set(qn('w:eastAsia'), '宋体')
    create_cover(doc, '系统测试计划')

    hdr(doc, '1. 测试范围与主要内容')
    tests = [
        ('功能测试', '验证以下核心功能正确性：用户注册/登录/登出、JWT Token认证与过期处理、硬件信息浏览与分类筛选、CPU/显卡/主板排行计算、推荐算法输出正确性、评测发布/查看/删除、评论发布/删除、收藏/取消收藏、管理员硬件CRUD、评测/评论审核、图片上传、Android WebView API代理与界面渲染。'),
        ('健壮性测试', '验证系统在异常输入下的容错能力：空值输入处理、超长文本截断、非法token拒绝、过期token自动跳转登录页、数据库连接断开后优雅降级、大文件上传（>10MB）拒绝。'),
        ('性能测试', '验证正常负载下响应时间：API响应<500ms（硬件列表/排行/评测列表）、推荐算法计算<2s、图片上传<3s（5MB以内）、页面首屏<2s。'),
        ('用户界面测试', '验证Web前端和Android端界面一致性：桌面端响应式布局、移动端WebView渲染正确性、表单验证提示、SweetAlert2弹窗交互、Chart.js图表渲染、图片灯箱缩放、管理员暗色主题。'),
        ('安全性测试', '验证安全防护：未登录访问受保护API返回401、非管理员访问/admin/**返回403、JWT Token伪造检测、SQL注入防护、XSS跨站脚本防护、BCrypt密码加密验证、文件上传类型校验（仅允许image/*）。'),
        ('兼容性测试', '验证多环境兼容性：Chrome/Firefox/Edge浏览器、Android 7.0+ WebView、Windows/MacOS后端、MySQL 8.0数据库。'),
    ]
    for name, desc in tests:
        para(doc, f'{name}：{desc}')

    hdr(doc, '2. 测试方法')
    para(doc, '本系统采用黑盒测试为主、白盒测试为辅的混合测试策略：')
    para(doc, '（1）黑盒测试：基于需求规格说明书设计测试用例，覆盖所有功能点。适用于功能测试、界面测试、兼容性测试。')
    para(doc, '（2）白盒测试：针对核心算法模块（推荐算法、JWT认证、密码加密）进行代码级单元测试。使用JUnit 5 + Mockito框架。')
    para(doc, '（3）集成测试：使用Spring Boot Test框架对Controller层进行集成测试，验证完整请求-响应链路。')
    para(doc, '（4）手动探索性测试：对Android端WebView渲染、用户交互流程进行手动测试。')

    hdr(doc, '3. 测试环境与测试辅助工具')
    make_table(doc, ['测试环境', '说明'], [
        ('后端环境', 'Windows 11 + JDK 21 + Spring Boot 3.2.2 + Maven'),
        ('数据库', 'MySQL 8.0.36 (localhost:3306)'),
        ('前端环境', 'Vue 3 + Vite 8 + Node.js 22 + Chrome DevTools'),
        ('Android环境', 'Android Studio + SDK 35 + 模拟器(Pixel 7) + 真机'),
        ('API测试工具', 'Postman / curl'),
        ('单元测试', 'JUnit 5 + Mockito + Spring Boot Test'),
        ('性能分析', 'Chrome DevTools Network + Spring Boot Actuator'),
        ('版本管理', 'Git + GitHub (xianyan666/pc-advisor)'),
    ])

    hdr(doc, '4. 测试完成准则')
    para(doc, '（1）功能性测试用例通过率达到100%（所有核心功能全部通过）。')
    para(doc, '（2）非功能性测试用例通过率≥95%（UI兼容性、性能指标、安全性测试）。')
    para(doc, '（3）无P0/P1级别缺陷（不存在系统崩溃、数据丢失、安全漏洞）。')
    para(doc, '（4）回归测试通过（所有已修复缺陷的回归测试用例通过）。')

    hdr(doc, '5. 人员与任务表')
    make_table(doc, ['人员', '角色', '职责、任务', '时间'], [
        ('李锦轩', '测试负责人', '制定测试计划，编写测试用例，后端API测试，性能测试', '2026.05.21-05.25'),
        ('李俊泽', '测试工程师', '前端功能测试，UI兼容性测试，Android端测试', '2026.05.21-05.25'),
        ('黄国政', '测试工程师', '安全性测试，数据库测试，集成测试，回归测试', '2026.05.21-05.25'),
        ('陈海霞', '指导老师', '测试方案审核，测试报告评审', '2026.05.26'),
    ])

    path = os.path.join(OUTPUT_DIR, '001 A 电脑硬件分析推荐系统 系统测试计划.docx')
    doc.save(path)
    print('Created: 系统测试计划')

# ============================================================
# Report 3: 结项报告
# ============================================================
def create_closure_report():
    doc = Document()
    style = doc.styles['Normal']
    font = style.font; font.name = '宋体'; font.size = Pt(11)
    style.element.rPr.rFonts.set(qn('w:eastAsia'), '宋体')

    for _ in range(4):
        doc.add_paragraph()
    p = doc.add_paragraph(); p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    run = p.add_run('{ ' + PROJECT_NAME + ' }')
    run.font.size = Pt(22); run.font.name = '黑体'
    run._element.rPr.rFonts.set(qn('w:eastAsia'), '黑体'); run.bold = True
    doc.add_paragraph(); doc.add_paragraph()
    p = doc.add_paragraph(); p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    run = p.add_run('结项报告')
    run.font.size = Pt(26); run.font.name = '黑体'
    run._element.rPr.rFonts.set(qn('w:eastAsia'), '黑体'); run.bold = True
    for _ in range(4):
        doc.add_paragraph()

    t = doc.add_table(rows=7, cols=2); t.style = 'Table Grid'
    for i, (k, v) in enumerate([
        ('项目名称', PROJECT_NAME), ('项目编号', '101 B'),
        ('本文件标识符', 'Company-Project-PCM-REVIEW'),
        ('项目承担部门', SCHOOL), ('项目经理', '李锦轩'),
        ('立项时间', '2025年9月'), ('开发完成时间', '2026年5月'),
    ]):
        set_cell_font(t.cell(i, 0), k, bold=True)
        set_cell_font(t.cell(i, 1), v)
    doc.add_page_break()

    hdr(doc, '1. 项目综合评估')
    hdr(doc, '1.1 项目完成情况评估', 2)
    make_table(doc, ['计划', '实际完成情况'], [
        ('需求分析与获取', '完成需求分析报告和需求获取报告，整理功能需求22项。'),
        ('系统设计', '确定Spring Boot+Vue 3技术栈，设计三层架构，绘制系统架构图。'),
        ('数据库设计', '完成11张表的完整数据库设计，采用"主表+详情子表"架构。'),
        ('后端开发', '完成9个Controller共30+个API接口开发：用户认证、硬件管理、排行算法、推荐算法、评测评论、收藏、管理员审核。'),
        ('前端开发', '完成11个Vue页面组件：首页/硬件浏览/推荐/排行/评测/用户中心/管理员后台，响应式布局。'),
        ('推荐算法', '实现双层筛选推荐算法：硬性条件（游戏/预算/核心数）+软性条件（品牌/系列/板型）打分排序。'),
        ('管理员后台', '实现暗色风格管理后台：仪表盘统计、硬件CRUD、评测/评论审核。'),
        ('Android APP', '完成WebView封装APP：本地资源加载、API代理重写、服务器地址配置，APK 6.9MB。'),
        ('测试验证', '后端API测试通过，Android局域网通信测试通过，前端页面渲染正常。'),
        ('代码管理', 'Git仓库初始化并推送GitHub（xianyan666/pc-advisor），共325个文件，多次迭代提交。'),
        ('文档编写', '完成开题报告、需求分析/获取报告、系统设计报告、数据库设计报告、测试计划、项目计划、结项报告共8份文档。'),
    ])

    hdr(doc, '1.2 项目质量评估', 2)
    make_table(doc, ['主要质量属性', '评估'], [
        ('正确性', '核心功能经人工测试验证，结果符合预期。'),
        ('健壮性', '全局异常处理器覆盖认证/授权/业务/资源等场景，返回统一Result格式。'),
        ('可靠性', '基于Spring Boot+MySQL成熟技术栈，JWT无状态认证。'),
        ('性能/效率', 'API响应<100ms，推荐计算<200ms完成。'),
        ('易用性', '直观卡片式布局和色彩编码（Intel蓝/AMD红/NVIDIA绿），Android一键连接配置。'),
        ('清晰性', '后端Controller-Service-Mapper-Entity分层清晰，前端组件化开发，命名规范统一。'),
        ('安全性', 'BCrypt密码加密、JWT Token认证、Spring Security权限控制、参数化SQL防注入。'),
        ('可扩展性', '硬件类型通过枚举管理，新增类型只需添加枚举值和详情表。'),
        ('兼容性', '兼容Chrome/Firefox/Edge浏览器和Android WebView(API 24+)。'),
        ('可移植性', '基于JVM运行，前后端独立部署，MySQL标准SQL。'),
    ])

    hdr(doc, '1.3 项目的市场价值', 2)
    para(doc, '（1）解决用户痛点：电脑硬件参数复杂，普通消费者难以做出最优选择。系统通过排行和推荐算法将复杂参数转化为直观评分和兼容方案，大幅降低选购门槛。')
    para(doc, '（2）社区互动价值：评测/评论/收藏功能建立硬件爱好者社区，管理员审核机制保证内容质量。')
    para(doc, '（3）移动端覆盖：Android APP使系统可在移动场景使用，扩大用户群体。')
    para(doc, '（4）数据资产：积累的硬件数据库和评测内容具有长期价值，可为后续AI功能提供训练数据。')

    hdr(doc, '1.4 项目开发带来的个人成长', 2)
    para(doc, '（1）全栈开发能力：掌握Spring Boot后端、Vue 3前端、Android WebView封装、MySQL数据库设计的完整技术链。')
    para(doc, '（2）架构设计思维：理解分层架构、RESTful API设计、JWT认证方案、数据库范式设计等核心概念。')
    para(doc, '（3）工程实践能力：实际运用Git版本控制、Gradle/Maven构建、npm包管理、IDE开发工具。')
    para(doc, '（4）问题排查能力：在Android WebView空白页面问题上，经历了file:// CORS限制、ES Module兼容性等多个技术排查环节。')
    para(doc, '（5）文档撰写能力：完成8份规范技术文档，涵盖从需求分析到结项总结的完整软件工程流程。')

    hdr(doc, '2. 经验教训总结')
    make_table(doc, ['序号', '经验教训'], [
        ('1', '技术选型前充分调研兼容性——Android WebView对ES Module的file://协议支持存在限制，改用loadDataWithBaseURL+虚拟域名方案解决。建议在选型时优先考虑CORS和ES Module兼容性。'),
        ('2', '前后端分离需统一错误处理——初期JWT过期时后端返回403，但前端只拦截401。建议开发初期约定好统一的HTTP错误码和响应格式。'),
        ('3', '文件上传注意框架默认行为差异——MultipartFile.transferTo()在Windows和Linux下行为不一致，改用Files.copy(InputStream, Path)统一处理。'),
        ('4', '数据库采用"主表+子表"模式——CPU/显卡/主板详情拆分到独立子表，比单一宽表更灵活，新增硬件类型只需加表。'),
        ('5', 'Android端优先使用内置资源——产品图片直接打包进APK assets，避免不必要网络请求，仅将动态内容代理到服务器。'),
        ('6', '文档与开发同步——前期文档在开发前完成，后期文档在开发过程中逐步完善，避免项目结束时赶工。'),
    ])

    path = os.path.join(OUTPUT_DIR, '001 A 电脑硬件分析推荐系统 结项报告.docx')
    doc.save(path)
    print('Created: 结项报告')

# ============================================================
# Report 4: 项目计划
# ============================================================
def create_project_plan():
    doc = Document()
    style = doc.styles['Normal']
    font = style.font; font.name = '宋体'; font.size = Pt(11)
    style.element.rPr.rFonts.set(qn('w:eastAsia'), '宋体')
    create_cover(doc, '项目计划')

    hdr(doc, '0. 文档介绍')
    hdr(doc, '0.1 文档目的', 2)
    para(doc, '制定项目计划，明确项目范围、目标、过程模型、资源分配和进度安排，作为项目团队在整个开发周期中的行动纲领。', indent=True)
    hdr(doc, '0.2 文档范围', 2)
    para(doc, '涵盖完整生命周期：需求分析、系统设计、编码实现、测试验证、部署交付和文档撰写。', indent=True)
    hdr(doc, '0.3 读者对象', 2)
    para(doc, '项目团队成员、指导老师、项目评审专家。', indent=True)
    hdr(doc, '0.4 参考文献', 2)
    para(doc, '[SPP-PROC-PP] SEPG，《项目规划规范》，南京航空航天大学，2025')
    hdr(doc, '0.5 术语与缩写解释', 2)
    make_table(doc, ['缩写/术语', '解释'], [
        ('SPP', '精简并行过程，Simplified Parallel Process'),
        ('PP', '项目规划，Project Planning'),
        ('WBS', '工作分解结构，Work Breakdown Structure'),
        ('MVP', '最小可行产品，Minimum Viable Product'),
    ])

    hdr(doc, '1. 项目介绍')
    hdr(doc, '1.1 项目范围', 2)
    para(doc, '本项目是面向电脑硬件选购场景的Web+移动双端推荐系统。通过分析用户需求（游戏/办公、预算、核心数等），从数据库中匹配兼容的CPU/显卡/主板组合，提供性价比评分排序。同时提供硬件参数浏览、排行计算、评测社区、管理员审核后台等功能。', indent=True)
    para(doc, '包含：用户认证、硬件数据库、排行算法、推荐算法、评测评论、收藏、管理员审核后台、Android APP。')
    para(doc, '不包含：电子商务交易、在线支付、硬件库存管理、订单物流跟踪。')
    hdr(doc, '1.2 项目目标', 2)
    para(doc, '（1）完成可交付的Web端系统，包含11个功能页面。')
    para(doc, '（2）完成Android端APP封装，APK体积<10MB，支持局域网连接。')
    para(doc, '（3）推荐算法覆盖至少20款CPU×10款GPU×10款主板的组合空间。')
    para(doc, '（4）API响应时间<500ms，推荐计算时间<2s。')
    para(doc, '（5）完成8份规范技术文档。')
    hdr(doc, '1.3 客户与最终用户介绍', 2)
    para(doc, '目标用户：具有电脑硬件选购需求的普通消费者（游戏玩家、设计师、办公用户）、DIY爱好者、电脑装机店和硬件经销商。该项目为南京航空航天大学计算机科学与技术学院大学生科技创新基金项目。')

    hdr(doc, '2. 项目过程定义')
    hdr(doc, '2.1 过程模型', 2)
    para(doc, '采用基于SPP（精简并行过程）裁剪的迭代开发模型：')
    para(doc, '（1）需求阶段（2025.09-2025.10）：用户调研、竞品分析，编写需求分析/获取报告。')
    para(doc, '（2）设计阶段（2025.10-2025.11）：系统架构设计、技术选型、数据库设计，编写系统设计报告。')
    para(doc, '（3）开发阶段（2025.11-2026.04）：分模块迭代开发后端API、前端页面和Android APP。')
    para(doc, '（4）测试阶段（2026.05）：功能测试、性能测试和兼容性测试。')
    para(doc, '（5）交付阶段（2026.05）：文档整理、代码提交和项目验收。')
    hdr(doc, '2.2 方法与工具', 2)
    make_table(doc, ['过程域', '方法与工具'], [
        ('项目管理', 'Git + GitHub 版本控制'),
        ('后端开发', 'IntelliJ IDEA + Spring Boot 3.2.2 + Maven'),
        ('前端开发', 'VS Code + Vue 3 + Vite + Tailwind CSS'),
        ('数据库', 'MySQL 8.0 + Navicat + MyBatis-Plus'),
        ('API测试', 'Postman + curl'),
        ('Android开发', 'Android Studio + Kotlin + Gradle'),
        ('文档编写', 'Microsoft Word + Markdown'),
        ('UML建模', 'Draw.io (ER图/架构图)'),
        ('运行时环境', 'JDK 21 + Node.js 22 + Android SDK 35'),
    ])

    hdr(doc, '3. 人力资源计划')
    make_table(doc, ['角色', '职责', '人员姓名', '贡献度(%)'], [
        ('项目经理/后端开发', '项目规划、后端架构、API开发、数据库设计', '李锦轩', '40'),
        ('前端开发/测试', 'Vue前端开发、UI实现、接口联调、前端测试', '李俊泽', '30'),
        ('Android开发/文档', 'Android APP开发、文档编写、系统测试', '黄国政', '30'),
        ('指导老师', '技术指导、方案审核、进度监督', '陈海霞', '—'),
    ])

    hdr(doc, '4. 软硬件资源计划')
    make_table(doc, ['软硬件资源名称', '级别', '详细配置', '获取方式与时间', '使用说明'], [
        ('开发主机(PC)', '关键', 'Windows 11, 16GB RAM', '已有', '全周期使用'),
        ('MySQL数据库', '关键', 'MySQL 8.0.36', '已有（免费）', '数据存储与管理'),
        ('IntelliJ IDEA', '关键', 'Community 2024', '已有（免费）', '后端Java开发'),
        ('Android Studio', '关键', 'Latest Stable', '已有（免费）', 'Android APP开发'),
        ('GitHub仓库', '关键', 'Public Repository', '免费创建', '代码托管与版本控制'),
        ('Android设备', '普通', '模拟器+真机', '已有', 'APP功能测试'),
        ('Postman', '普通', '免费版', '已有（免费）', 'API调试'),
        ('Navicat', '普通', '免费版', '已有', '数据库管理'),
    ])

    hdr(doc, '5. 财务计划')
    make_table(doc, ['开支类别', '主要开支项、用途', '金额', '时间'], [
        ('软件工具', '开发工具均使用免费/社区版', '0元', '2025.09'),
        ('云服务', 'GitHub代码托管（免费）', '0元', '全周期'),
        ('测试设备', '利用已有个人设备', '0元', '2026.05'),
        ('文档打印', '项目报告纸质打印装订', '约50元', '2026.05'),
    ])
    para(doc, '说明：本项目以开源免费工具为主，整体财务开销极低。主要开支为文档打印费用。')

    hdr(doc, '6. 任务与进度')
    make_table(doc, ['任务名称', '起止时间', '工作人员', '工作量', '预期工作成果'], [
        ('需求分析与获取', '2025.09-2025.10', '李锦轩、李俊泽、黄国政', '4周', '需求分析报告、需求获取报告'),
        ('系统设计', '2025.10-2025.11', '李锦轩', '4周', '系统设计报告、技术选型方案'),
        ('数据库设计', '2025.11', '李锦轩', '2周', 'init.sql、数据库设计报告'),
        ('用户认证模块', '2025.11-2025.12', '李锦轩', '2周', '登录/注册/JWT/权限控制'),
        ('硬件管理与排行', '2025.12-2026.01', '李锦轩', '3周', '硬件CRUD、排行算法、推荐算法'),
        ('前端页面开发', '2026.01-2026.03', '李俊泽', '8周', '11个Vue页面组件'),
        ('评测评论系统', '2026.03-2026.04', '李锦轩、李俊泽', '3周', '评测/评论/收藏/图片上传'),
        ('管理员后台', '2026.04', '李俊泽', '2周', '仪表盘/审核/硬件管理'),
        ('Android APP', '2026.05', '黄国政', '2周', 'Android APK（6.9MB）'),
        ('测试与修复', '2026.05', '李锦轩、李俊泽、黄国政', '2周', '测试报告、Bug修复'),
        ('文档整理', '2026.05', '黄国政', '1周', '8份技术文档'),
        ('项目验收', '2026.05', '全体', '1周', '项目演示、结项报告'),
    ])

    path = os.path.join(OUTPUT_DIR, '001 A 电脑硬件分析推荐系统 项目计划.docx')
    doc.save(path)
    print('Created: 项目计划')

# ============================================================
if __name__ == '__main__':
    os.makedirs(OUTPUT_DIR, exist_ok=True)
    create_db_report()
    create_test_plan()
    create_closure_report()
    create_project_plan()
    print('Done! All 4 reports generated.')
