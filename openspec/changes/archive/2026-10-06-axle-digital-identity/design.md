## Context

本 change 是检修系统的第一个 capability，此时 `backend/` 与 `frontend/` 均尚未建立。数据模型与枚举的权威来源是 `prototype/`（已定稿静态原型），但 prototype 自身在"轮轴状态"上存在词汇混用（在修 / 在修中 / 检修中 / 预警 / 正常 / 报废 并存）。本设计对这些冲突做一次裁决并固化。技术栈按项目约束：后端 SpringBoot、前端 Vue3、数据库 MySQL、对象存储 MinIO（本模块暂不涉及 MinIO）。

## Goals / Non-Goals

**Goals:**
- 固化轮轴实体的字段字典与枚举字典，作为后端表结构与前端页面的单一事实源。
- 确立双状态模型（生命周期状态 + 检修流程状态 + 派生预警标志），消除 prototype 的状态词汇冲突。

**Non-Goals:**
- 不设计新建轮轴的录入 UI（MVP 假设数据由外部灌入，见 proposal）。
- 不设计 HMIS / HCCBM 的真实对接契约（仅留接口抽象，用 mock）。
- 不展开修程（例行 / 中修）的完整检修流程语义——归生产调度模块。

## Decisions

### 决策 1：状态拆分为两个正交字段 + 派生预警标志

`当前状态` 拆为 `lifecycle_status` 与 `repair_status`，预警 `alert` 为派生标志。

- **理由**：prototype 表格用"在修/待复检/预警/已出库"（检修流程），README SQL 用"在役/检修/报废"（生命周期），二者正交却压进一个字段；statusMap 里孤立的"报废"证明生命周期维度缺失。
- **备选**：保留单一状态枚举（凑齐所有词）——被否，因"预警"是标志而非状态、"报废"与"在修"可共存，单字段无法表达叠加关系。

### 决策 2：字段字典（轮轴实体）

| 中文名 | 字段名 | 类型 | 说明 |
|---|---|---|---|
| 轮轴 ID | `axle_id` | varchar(32) PK | 格式 `AX-YYYYMMDD-NNN`，导入时生成 |
| 车型 | `model` | varchar(32) | 如 CW-200 |
| 车号 | `train_no` | varchar(32) | 如 K65 |
| 生产厂家 | `factory` | varchar(64) | |
| 生产日期 | `produce_date` | date | |
| 装机日期 | `install_date` | date | 可空 = 已出厂未装机 |
| 生命周期状态 | `lifecycle_status` | tinyint | 见枚举字典 |
| 检修流程状态 | `repair_status` | tinyint | 仅生命周期=检修 时有意义 |
| 累计检修 | `repair_count` | int | 派生 |
| 剩余寿命 | `remaining_life` | int | 单位 km，派生 |

关键检测指标（随设备采集更新，本模块只读展示）：

| 中文名 | 字段名 | 类型 |
|---|---|---|
| 轮径磨损 | `wear` | decimal(5,2) mm |
| 裂纹深度 | `crack` | decimal(5,2) mm |
| 振动值 | `vibration` | decimal(5,2) mm/s |
| 温升 | `temp` | decimal(5,2) ℃ |

### 决策 3：枚举字典

| 枚举 | 取值（中文显示值） | 存储 |
|---|---|---|
| `lifecycle_status` | 在役 / 检修 / 报废 | tinyint 0/1/2 |
| `repair_status` | 在修 / 待复检 / 待验收 / 已出库 | tinyint 0/1/2/3 |
| `alert_type` | 参数超标 / 超期未修 / 临近报废 | tinyint |
| 修程（`repair_grade`） | 例行 / 中修 | tinyint |
| 履历事件（`event_type`） | 生产出厂登记 / 装车上线 / 复检处理 / 修复验收 / 报废 | tinyint |

- 归一化说明：prototype 的"在修中/检修中"并入"在修"；"正常"= 生命周期"在役"且无预警的投影，不单独存储；status=预警 的样本轮轴落为 生命周期=检修 + 检修流程=在修 + alert=参数超标。
- 修程只保留"例行/中修"两级，"复检/预警处置/完工"归入检修流程状态或预警标志，不再作为修程。

### 决策 4：履历采用事件表

履历独立成表 `axle_history`，按时间倒序展示，事件类型见枚举字典。一条履历 = 一个事件类型 + 事件时间 + 说明。状态更新动作写入履历（事件类型取与状态变更最贴近的一类）。

### 决策 5：ID 生成与数据入口

- 唯一 ID 在数据导入 / 外部灌入（HCCBM 入向 mock）时生成，格式 `AX-YYYYMMDD-NNN`，MVP 不提供手动新建轮轴的 UI。
- 后端暴露查询接口（列表、详情、履历）与状态更新接口；对国铁系统仅保留接口抽象，用 mock 数据。

### 决策 6：前后端分层

- 后端 SpringBoot + MySQL，`backend/` 目录；前端 Vue3 重写 prototype 对应页面（总览列表、轮轴详情），`frontend/` 目录。
- 密钥等外部配置统一放 `.env`，不入库。

## Risks / Trade-offs

- [prototype 状态词汇冲突已裁决，但若后续原型补页可能再变] → 以本字典为单一事实源，原型变动先改本字典。
- [报废与在役的过渡语义（报废后能否撤销）未定义] → MVP 阶段只做单向标记，撤销留待后续，spec 未约束。
- [双字段增加了列表筛选复杂度（筛选按钮混用流程状态与预警标志）] → 前端把"预警"筛选项映射为 alert 标志过滤，其余映射 repair_status，设计已明确。
