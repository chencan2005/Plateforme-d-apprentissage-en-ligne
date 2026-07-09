# 智慧教学平台（Online Learning Platform）

前后端分离的在线学习演示项目：学生选课学习、教师课程/作业管理、公告、AI 问答与知识点总结。

## 技术栈

| 层 | 技术 |
|----|------|
| 前端 | Vue 3 + TypeScript + Vite + Element Plus + Pinia |
| 后端 | Spring Boot 3 + MyBatis-Plus + JWT |
| 数据库 | MySQL 8 |
| AI | 阿里云 DashScope（通义千问）+ Spring AI Alibaba Graph |

> 前端目录名为 `vue3/frond-end`（历史拼写，请勿随意重命名以免破坏本地路径）。

## 快速启动

### 1. 数据库

1. 创建库：`online_learning_platform`
2. 导入 SQL：桌面上的 [`online_learning_platform.sql`](../online_learning_platform.sql)（或项目内同名脚本）

### 2. 后端

```bash
cd OnlineLearningPlatform
```

复制本地配置：

```bash
cp src/main/resources/application-local.yml.example src/main/resources/application-local.yml
```

编辑 `application-local.yml`，填写：

- MySQL 账号密码
- DashScope API Key（AI 功能需要；可不配，仅影响 AI 相关接口）
- JWT secret（建议改成至少 32 位随机串）

然后启动：

```bash
mvn spring-boot:run
```

默认端口：`8081`

### 3. 前端

```bash
cd vue3/frond-end
npm install
npm run dev
```

默认地址：`http://localhost:5173`（开发环境通过 Vite 代理访问 `/api` → `8081`）

生产构建请参考 `.env.production.example` 配置 `VITE_API_BASE_URL`。

## 演示账号

密码除特别说明外均为 `123456`（MD5 存储，仅供演示）。

| 角色 | 用户名 | 说明 |
|------|--------|------|
| 教师 | `teacher_zhang` | Java / Spring Boot / MySQL 课程 |
| 教师 | `teacher_li` | 前端 / Python / 算法课程 |
| 教师 | `teacher_chen` | Vue3 课程（密码为原账号密码） |
| 学生 | `student_wang` | 已选多门课，有作业与总结 |
| 学生 | `student_zhao` | 前端相关学习数据 |
| 学生 | `student_liu` | Spring Boot / Python |
| 学生 | `student_sun` | MySQL / 前端 |
| 学生 | `chencan` | AI 对话演示账号（密码为原账号密码） |

## 功能清单

**学生**

- 浏览/选修课程，查看章节并标记已学
- 提交作业、查看成绩与评语
- 公告中心、AI 智能问答、知识点总结（上传文档）

**教师**

- 发布/编辑课程与章节
- 发布作业、查看提交进度、人工/AI 批改
- 发布系统或课程公告、查看选课学生

## 建议演示路径

1. `teacher_zhang` / `123456` → 课程管理 → 章节 → 作业管理 → 批改
2. `student_wang` / `123456` → 课程中心 → 进入学习 → 交作业 → 成绩/公告
3. `chencan` → AI 智能问答（需配置 API Key）

## 已知限制（演示说明）

- 密码使用无盐 MD5，**不适合生产环境**
- 注册页可自选教师角色，演示用；正式环境应改为审核制
- AI 能力依赖 DashScope Key；未配置时相关接口会返回友好错误
- 作业模板与提交仍按「题目 + 截止时间」关联，未引入独立 templateId

## 目录结构

```
project/
├── OnlineLearningPlatform/   # Spring Boot 后端
├── vue3/frond-end/           # Vue3 前端
└── README.md
```
