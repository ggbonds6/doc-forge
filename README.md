# Doc Forge

党政机关公文标准化生产平台，面向通知、请示、函、报告等正式公文场景。

## 目录

- `backend/`：Spring Boot 后端，负责模板、文稿、合规校验、DOCX 导出。
- `frontend/`：React 前端，负责模板管理、文稿编辑、实时预览。

## 本地开发

### 后端

```bash
cd backend
mvn spring-boot:run
```

默认会使用内存 H2 数据库；生产环境可通过环境变量切换为 PostgreSQL：

- `DOCFORGE_DATASOURCE_URL`
- `DOCFORGE_DATASOURCE_USERNAME`
- `DOCFORGE_DATASOURCE_PASSWORD`
- `DOCFORGE_DATASOURCE_DRIVER`
- `DOCFORGE_HIBERNATE_DIALECT`

管理员账号配置：

- `DOCFORGE_ADMIN_USERNAME`
- `DOCFORGE_ADMIN_PASSWORD`
- `DOCFORGE_ADMIN_TOKEN`

### 前端

```bash
cd frontend
pnpm install
pnpm dev
```

默认访问：

- 前端：`http://localhost:5173`
- 后端：`http://localhost:8080`
