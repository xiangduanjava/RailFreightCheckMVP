## Context

已归档的 `axle-digital-identity` 能力已落地后端（SpringBoot 3.3 + JPA/Hibernate，H2 开发库 `create-drop`、MySQL 生产库 `ddl-auto: update`）与前端（Vue3 + Vue Router + axios）。轮轴实体 `Axle` 与枚举 `RepairGrade`（例行/中修）已就绪，列表/详情/状态更新接口已可用。本次为系统补上「新建轮轴」入口，动笔前已读 `prototype/axle-new.html` 与 `prototype/axle-new.js`（页面结构、字段、枚举、必填项的权威来源）。

## Goals / Non-Goals

**Goals:**
- 固化新建轮轴的新增字段字典与修程枚举字典，作为后端表结构与前端页面的单一事实源。
- 确立创建轮轴的接口契约（请求/响应、必填校验、初始状态、ID 生成、履历写入）。

**Non-Goals:**
- 不设计「下一步：检测参数」步骤（wear/crack/vibration/temp 仍由设备采集模块覆盖）。
- 不把新增字段回填到详情页/列表页展示（本次仅存储，后续模块按需展示）。
- 不涉及 HMIS / HCCBM 真实对接。

## Decisions

### 决策 1：新增字段字典（轮轴实体扩展）

在既有 `axle_info` 表（`Axle` 实体）上新增以下字段，均来自 `prototype/axle-new.html`：

| 中文名 | Java 字段 | 列名 | 类型 | 说明 |
|---|---|---|---|---|
| 轴位 | `axlePos` | `axle_pos` | varchar(16) | 枚举：1位轴 / 2位轴 / 3位轴 / 4位轴 |
| 左右侧 | `side` | `side` | varchar(8) | 枚举：左侧 / 右侧 |
| 轴承型号 | `bearing` | `bearing` | varchar(32) | 如 353130B |
| 钢号/材质 | `material` | `material` | varchar(32) | 50钢 / 35CrMo / EA4T |
| 出厂编号 | `serialNo` | `serial_no` | varchar(64) | |
| 合格证编号 | `certNo` | `cert_no` | varchar(64) | |
| 公称轮径 | `nominalWheel` | `nominal_wheel` | decimal(6,2) | mm |
| 实测轮径 | `actualWheel` | `actual_wheel` | decimal(6,2) | mm |
| 轴颈直径 | `journalDia` | `journal_dia` | decimal(6,2) | mm |
| 轮缘厚度 | `flangeThick` | `flange_thick` | decimal(6,2) | mm |
| 轮辋厚度 | `rimThick` | `rim_thick` | decimal(6,2) | mm |
| 轮对内侧距 | `backGauge` | `back_gauge` | decimal(6,2) | mm |
| 备注 | `remark` | `remark` | varchar(512) | |

- **理由**：这些是新建录入独有的基础属性，与既有 `Axle` 字段（model/trainNo/factory/produceDate/installDate/status 等）同表存放，MVP 无需拆表。
- **轴位 / 左右侧 / 材质用 varchar 存中文显示值**（而非新增 Java 枚举）——它们是描述性属性、无业务状态流转逻辑，存显示值即可与 prototype 字面一致，避免枚举膨胀；白名单校验在服务层完成（见决策 4）。**备选**：仿照 `RepairGrade` 新增三个 Java 枚举（ORDINAL）——被否，因这些字段无行为、且中文枚举名可读性差。

### 决策 2：修程枚举扩展（用户已确认「扩展为四级」）

`RepairGrade` 由「例行/中修」扩展为：

| 枚举常量 | 存储 | 显示值 |
|---|---|---|
| `ROUTINE` | 0 | 例行 |
| `MIDDLE` | 1 | 中修 |
| `MAJOR` | 2 | 大修 |
| `REINSPECT` | 3 | 复检 |

- 追加值按顺序排在末尾，不改动既有 0/1 的存储值，保证存量数据兼容。
- 表单里的「例行检查」在服务层归一为 `ROUTINE`（显示值「例行」），其余字面值一一对应。
- **理由**：`prototype/axle-new.html` 的修程有 4 个选项，且列表页「修程」列复用 `RepairGrade` 显示，扩展同一枚举是改动面最小、语义一致的做法。

### 决策 3：创建接口契约

新增 `POST /api/axles`，请求体 `CreateAxleRequest`（camelCase 字段名，枚举类字段用中文显示值，与 `UpdateStatusRequest` 风格一致）：

| 字段 | 类型 | 必填 |
|---|---|---|
| `model` | String | 是 |
| `trainNo` | String | 是 |
| `repair` | String（例行/中修/大修/复检，容忍「例行检查」） | 是 |
| `axlePos` | String（1位轴/2位轴/3位轴/4位轴） | 是 |
| `factory` | String | 是 |
| `produceDate` | LocalDate | 是 |
| `nominalWheel` | BigDecimal | 是 |
| `actualWheel` | BigDecimal | 是 |
| `side` / `bearing` / `material` / `serialNo` / `certNo` / `installDate` / `journalDia` / `flangeThick` / `rimThick` / `backGauge` / `remark` | 各自类型 | 否 |

响应：返回创建后的轮轴详情（复用 `AxleDetail`，含新生成的 `axleId`），前端据此跳转详情页。

- **理由**：字段名与 entity 一致减少转换；枚举类字段沿用中文显示值，与既有 `UpdateStatusRequest` 的「生命周期/检修流程状态」传中文标签的做法对齐。

### 决策 4：服务端校验与初始状态

- 必填字段用 `jakarta.validation` 注解（`@NotBlank` / `@NotNull`）在 Controller 层 `@Valid` 拦截，缺一返回 400 并提示缺失项（`pom.xml` 已有 `spring-boot-starter-validation`）。
- 枚举类字段（`repair`/`axlePos`/`side`/`material`）在服务层做白名单校验，非法值返回 400。
- 创建时固定：`lifecycleStatus=IN_SERVICE`（在役）、`repairStatus=null`、`repairCount=0`、`alertType=null`、检测指标（wear/crack/vibration/temp）为空、`latestDetection=null`。
- 写入一条 `PRODUCED`（生产出厂登记）履历，文案「轮轴制造完成，完成条码创建与基础参数录入。」与既有 DataSeeder 一致。

### 决策 5：唯一 ID 生成

服务端在创建时生成 `AX-YYYYMMDD-NNN`：
1. 取当前日期拼前缀 `AX-YYYYMMDD-`；
2. 查询该前缀下已存在的最大 `NNN` 序号，+1 补零到 3 位（无则从 `001` 起）；
3. 若因并发撞主键（`axle_id` 为主键），捕获冲突后重试一次。

- **理由**：序号自增确定性更强、无随机碰撞；主键唯一约束兜底并发。prototype 的随机 3 位仅是演示，不作为实现约束。

### 决策 6：前端页面与路由

- 新增 `frontend/src/views/AxleNewView.vue`，Vue3 重写 `prototype/axle-new.html`：表单字段/枚举/必填标记/右侧完整性进度与实时预览对齐原型。
- 新增路由 `/axles/new`（name `axle-new`），必须注册在 `/axles/:id` 之前，避免被 `:id` 吞掉。
- `frontend/src/api/axle.js` 新增 `createAxle(payload)`。
- `App.vue` 侧边栏新增「新建轮轴」导航项；列表页「轮轴数字身份」板块头部新增「新建轮轴」按钮（对齐 `prototype/index.html` 已加按钮），跳转 `/axles/new`。
- 「下一步：检测参数」在本页为占位按钮，点击提示「检测参数录入：后续模块实现」（沿用 `AxleListView.onNewTask` 的 stub 风格）。

## Risks / Trade-offs

- [修程「复检」与检修流程状态「待复检」语义接近，易混淆] → 二者正交（一个是修程等级、一个是流程步骤），UI 用字段名区分；spec 已按用户决策固化。
- [RepairGrade 用 ORDINAL 追加枚举值] → 只追加到末尾、不改既有顺序，存量 0/1 不变，兼容。
- [并发创建可能 ID 撞主键] → 主键唯一约束兜底 + 序号生成逻辑重试一次。
- [新增字段暂未在任何页面展示] → 本次仅入库，作为后续模块的数据储备，不影响既有页面。
- [轴位/左右侧/材质存字符串，可能录入非法值] → 前端 select 约束 + 服务层白名单校验双保险。

## Migration Plan

- 开发库 H2：`ddl-auto: create-drop`，重启自动重建表，无需迁移。
- 生产库 MySQL：`ddl-auto: update`，Hibernate 自动为 `axle_info` 追加新列；既有行新列为 NULL，不补历史数据（新字段仅对新建轮轴有意义）。
- 无需手工 SQL 或回滚脚本；若需回滚，回退代码版本即可（新列不影响旧逻辑）。

## Open Questions

- 新增字段后续是否需要在轮轴详情页展示（当前 `axle-detail.html` / `AxleDetailView.vue` 未展示），留待后续模块决定，不影响本次录入能力。
