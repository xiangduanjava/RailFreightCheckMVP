## Context

动机见 proposal.md - Why。当前后端已落地 `Axle` 实体（含 wear/crack/vibration/temp 四个检测指标字段，均暂为 null）、`AxleService`（`toEntity` 明确把这些指标留空待设备采集模块覆盖）、履历表 `axle_history`（事件类型 `EventType` 为 ORDINAL 枚举，当前 5 个值）、前端 Vue3（`App.vue` 侧边栏「设备采集」为 `nav-disabled`）。动笔前已读 `prototype/device.html`（工位设备状态 / 数据上传状态 / 采集日志三块面板，是页面结构与术语的权威参考）、`prototype/axle-new.html`/`.js`（必填校验、完整性进度、toast 弹窗的交互范式）。

## Goals / Non-Goals

**Goals:**
- 固化检测记录的表结构、工位枚举与「工位 → 必填检测项」映射，作为后端表结构与前端补录表单的单一事实源。
- 确立手动补录的接口契约（请求/响应、必填校验、回写规则、同步记录与汇总查询）。

**Non-Goals:**
- 不设计硬件设备自动读取（接口适配、驱动、边缘计算均不在本次范围）。
- 不设计断网本地缓存 / 离线队列 / 自动重传——PC 端实时同步，提交即落库。
- 不设计移动端（手持 APP / 小程序）。
- 不设计自动称重系统的重量数据（轮轴实体无重量字段）。

## Decisions

### 决策 1：新增检测记录表 `detection_record`

新增独立实体 `DetectionRecord` / 表 `detection_record`，每次手动补录写一条，字段如下：

| 中文名 | Java 字段 | 列名 | 类型 | 说明 |
|---|---|---|---|---|
| 主键 | `id` | `id` | bigint IDENTITY | 自增 |
| 轮轴 ID | `axleId` | `axle_id` | varchar(32) | 关联 `axle_info.axle_id` |
| 工位 | `station` | `station` | enum ORDINAL | 见决策 2 |
| 检测时间 | `detectionTime` | `detection_time` | datetime | 补录填写，默认当前时间 |
| 轮径磨损 | `wear` | `wear` | decimal(5,2) | 可空 |
| 裂纹深度 | `crack` | `crack` | decimal(5,2) | 可空 |
| 振动值 | `vibration` | `vibration` | decimal(5,2) | 可空 |
| 温升 | `temp` | `temp` | decimal(5,2) | 可空 |
| 备注 | `remark` | `remark` | varchar(255) | 可空 |
| 同步状态 | `syncStatus` | `sync_status` | enum ORDINAL | 见决策 7，MVP 恒为「已同步」 |
| 同步时间 | `syncTime` | `sync_time` | datetime | 落库时间 |

- **理由**：device.html 的「采集日志」与「数据上传状态」需要按条记录（工位、轮轴、检测值、同步状态）统计与展示；独立表把「一次采集事件」与「轮轴最新值快照」解耦，天然支撑日志倒序与汇总，且为后续硬件自动采集预留落点。
- **备选**：只回写 `Axle` 指标 + 写 `axle_history`，不建新表，日志从履历里 `event_type=检测` 反推。被否——履历 `note` 是自由文本，无法可靠统计「今日采集/已上传」等指标，也无法精确还原每条检测的四个数值。

### 决策 2：工位枚举 `Station` 与必填检测项映射

新增 Java 枚举 `Station`（ORDINAL），取值对齐 `prototype/device.html`：

| 枚举常量 | code | 显示值 | 必填检测项 |
|---|---|---|---|
| `WHEEL_DETECTOR` | 0 | 轮对尺寸检测机 | 轮径磨损（wear） |
| `ULTRASONIC` | 1 | 超声波探伤仪 | 裂纹深度（crack） |
| `WEIGHING` | 2 | 自动称重系统 | 无（重量不在 MVP） |
| `HANDHELD` | 3 | 手持终端补录 | 至少一项检测指标 |

- 补录表单的「工位」下拉只提供有检测项的三个工位（轮对尺寸检测机、超声波探伤仪、手持终端补录）；`WEIGHING` 仅出现在「工位设备状态」面板做静态展示（对齐 device.html 的四台设备），不作为补录目标。
- **理由**：工位携带「必填检测项」业务规则，用枚举承载 `label` + 必填项判定逻辑，比字符串白名单（`Set<String>` + `requireIn`）更能表达行为，也与 `RepairGrade`/`LifecycleStatus` 对「有业务含义的取值」用枚举的先例一致。
- **备选**：仿 `axlePos`/`side`/`material` 用 varchar 存中文 + 服务层白名单。被否——这三个是纯描述性属性、无业务流转；而工位决定必填项，有行为逻辑。

### 决策 3：履历事件类型扩展 `EventType.DETECTED`

`EventType` 追加一个值 `DETECTED(5, "检测记录")`，用于补录时写入轮轴履历。按 ORDINAL 追加到末尾，不改动既有 0–4 的存储值，保证存量数据兼容（与上次「修程枚举扩展」追加 MAJOR/REINSPECT 的做法一致）。

履历 note 格式：`「{工位显示值}」检测数据已上传`（如 `「轮对尺寸检测机」检测数据已上传`），与 DataSeeder 中 `latestDetection` 的短标签风格一致。

### 决策 4：回写 `Axle` 规则

补录提交成功后，回写该轮轴：

- **只覆盖本次填写的检测指标**：wear/crack/vibration/temp 中，请求体里非 null 的才更新，null 的保留轮轴原值（避免「探伤工位只录裂纹」时误把既有轮径磨损清空）。
- **最近检测来源 `latestDetection`**：按工位映射为短标签——轮对尺寸检测机 → `轮对尺寸检测`、超声波探伤仪 → `超声波探伤`、手持终端补录 → `手持补录`（对齐 DataSeeder 既有 `latestDetection` 取值风格）。
- 其余字段（状态、修程、履历事件类型等）不动。

- **理由**：「检测即上传、数据即共享」要求详情页/列表页立即反映最新数据，回写 `Axle` 让既有 `detail`/`list`/`toDetail` 无需改动即可展示；只覆盖填写项避免了跨工位误清空。

### 决策 5：后端接口契约

新增 `DetectionController`，前缀 `/api/detections`：

| 接口 | 说明 |
|---|---|
| `POST /api/detections` | 手动补录。请求体 `CreateDetectionRequest`（camelCase） |
| `GET /api/detections?limit=5` | 最近同步记录（时间倒序），默认 5 条 |
| `GET /api/detections/summary` | 上传状态汇总（今日采集 / 已上传 / 待同步） |

`CreateDetectionRequest` 字段：

| 字段 | 类型 | 必填 |
|---|---|---|
| `axleId` | String | 是（校验轮轴存在） |
| `station` | String（中文显示值） | 是（白名单校验） |
| `detectionTime` | LocalDateTime | 否（缺省为当前时间） |
| `wear` / `crack` / `vibration` / `temp` | BigDecimal | 否（按工位必填规则校验） |
| `remark` | String | 否 |

响应：返回保存后的 `AxleDetail`（复用既有 DTO，含更新后的检测指标与履历），前端据此刷新。字段命名、枚举类字段传中文显示值，沿用 `CreateAxleRequest`/`UpdateStatusRequest` 的风格。

### 决策 6：前端页面与路由

- 新增 `frontend/src/views/DeviceCollectionView.vue`，Vue3 重写 `prototype/device.html`，三块面板对齐原型：
  - **工位设备状态**：静态设备列表（四台设备 + 状态标签，硬件状态用 mock 静态值，不接真实设备）。
  - **数据上传状态**：调用 `summary` 接口展示今日采集 / 已上传 / 待同步（MVP 恒为 0）。
  - **采集日志**：调用列表接口，时间倒序展示最近 5 条同步记录。
- 新增「手动补录」入口（页头按钮），点击打开补录弹窗：轮轴 ID（输入）、工位（下拉，3 项）、检测时间（默认当前）、四个检测指标（按工位标注必填）、备注；前端先做完整性校验（缺失弹 toast 列出缺失项，对齐 `axle-new.js` 的 `validateRequired` + toast 范式），通过后调 `submitDetection`，成功后关闭弹窗并刷新日志/汇总。
- 新增路由 `/device`（name `device-collection`），注册于 `frontend/src/router/index.js`。
- 新增 `frontend/src/api/detection.js`：`submitDetection(payload)`、`listDetections(limit)`、`getDetectionSummary()`。
- `App.vue` 侧边栏把「设备采集」由 `nav-disabled` 改为 `router-link`，`active` 按 `route.name === 'device-collection'` 判定。

### 决策 7：实时同步语义（无离线队列）

PC 端补录为**提交即落库**：`POST /api/detections` 在同一个事务里写 `detection_record`、回写 `Axle`、写 `axle_history`，成功后记录 `syncStatus = SYNCED`。`sync_status` 字段保留 `PENDING` 值仅为未来硬件/离线场景预留，MVP 不产生 `PENDING`，故「待同步」恒为 0。

- **理由**：scope 明确「检测即上传、数据即共享，不需要后续人工补录」，PC 端天然在线，无需引入本地缓存与重传队列，符合 MVP「最小成本」。

## Risks / Trade-offs

- [检测记录表随采集量持续增长] → MVP 单表 + 索引即可，`summary` 只按日期范围聚合；后续量大再归档/分区。
- [`EventType`/`Station` 用 ORDINAL 追加值] → 只追加到末尾、不改既有顺序，存量 0–4/0–3 不变，兼容。
- [回写 `Axle` 可能覆盖历史检测值] → 只覆盖本次填写的指标，未填写的保留原值（决策 4），避免跨工位误清空。
- [自动称重系统重量缺失] → 明确非目标；仅在「工位设备状态」静态展示，不作为补录工位，避免出现「选了称重却没东西可录」的空场景。
- [补录工位枚举与 spec「工位取值 4 项」存在子集差异] → spec 描述设备分类枚举域（4 项），设计明确补录仅开放有检测项的 3 项，差异在「工位设备状态」面板与补录下拉的用途不同，非行为冲突。

## Migration Plan

- 开发库 H2：`ddl-auto: create-drop`，重启自动重建 `detection_record`，无需迁移。
- 生产库 MySQL：`ddl-auto: update`，Hibernate 自动创建 `detection_record`；`EventType` 追加 `DETECTED=5`、`Station` 为新增枚举，存量行不受影响。
- 无需手工 SQL 或回滚脚本；回滚即回退代码版本（新表/新枚举不影响旧逻辑）。

## Open Questions

- 补录表单的「轮轴」选择是否需要支持扫码枪输入 / 关键字联想下拉，还是仅文本框输入 ID——不影响本次能力，后续按现场反馈增强。
