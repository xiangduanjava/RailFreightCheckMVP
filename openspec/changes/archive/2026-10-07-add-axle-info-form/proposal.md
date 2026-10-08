## Why

已归档的「轮轴数字身份」模块把「新建轮轴录入」列为非目标，MVP 只能靠外部系统（HCCBM 入向 mock）或初始导入灌入轮轴数据，系统自身无法录入一根新轮轴。现在 `prototype/axle-new.html` 已定稿「新建轮轴 · 基础信息录入」表单，需要落地这一录入能力，让系统能自主新建轮轴、生成数字身份证，补齐数字身份管理闭环的第一环。

## What Changes

- 新增「新建轮轴基础信息录入」后端接口（`POST /api/axles`），按 `axle-new.html` 的字段创建一根新轮轴，并生成全局唯一 ID（格式 `AX-YYYYMMDD-NNN`，服务端生成，保证唯一）。
- 扩展轮轴实体的基础字段，对齐 `axle-new.html`：轴位、左右侧、轴承型号、钢号/材质、出厂编号、合格证编号、关键尺寸（公称轮径、实测轮径、轴颈直径、轮缘厚度、轮辋厚度、轮对内侧距）、备注。
- 扩展修程枚举 `repair_grade`：由「例行 / 中修」扩展为「例行 / 中修 / 大修 / 复检」（`axle-new.html` 的「例行检查」归一为「例行」）。
- 前端新增「新建轮轴」页面（Vue3 重写 `axle-new.html`），路由 `/axles/new`，并把列表页「轮轴数字身份」板块的「新建轮轴」按钮接入该页。
- 新建成功时写入一条「生产出厂登记」履历，轮轴初始生命周期状态为「在役」。

**非目标（本次不实现）**

- `axle-new.html` 的「下一步：检测参数」步骤（关键检测指标 wear/crack/vibration/temp 的录入）不做，仍由设备采集模块后续覆盖。
- 不实现 HMIS / HCCBM 真实对接，继续用 mock 数据。
- 不涉及移动端录入。

## Capabilities

### New Capabilities

<!-- 无新 capability：新建录入属于既有「轮轴数字身份管理」能力的补充，不单独拆能力 -->

### Modified Capabilities

- `axle-digital-identity`: 新增「新建轮轴基础信息录入」需求；扩展轮轴基础字段字典；扩展修程枚举取值。

## Impact

- 后端 `backend/`：扩展 `Axle` 实体与表结构（新增基础字段）、扩展 `RepairGrade` 枚举、新增创建轮轴的 DTO 与 Service/Controller 逻辑、新建时生成唯一 ID 并写「生产出厂登记」履历。
- 前端 `frontend/`：新增 `AxleNewView.vue` 页面、`/axles/new` 路由、`createAxle` API 封装；列表页新增「新建轮轴」入口按钮。
- 规格 `openspec/specs/axle-digital-identity/spec.md`：追加新建录入需求与修程枚举变更。
- 数据模型决策（新增字段字典 + 修程枚举字典）固化在 design.md。
