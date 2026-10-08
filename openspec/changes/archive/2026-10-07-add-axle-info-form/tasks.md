## 1. 后端：实体与枚举扩展

- [x] 1.1 扩展 `Axle` 实体，新增设计决策 1 的字段（axlePos/side/bearing/material/serialNo/certNo/nominalWheel/actualWheel/journalDia/flangeThick/rimThick/backGauge/remark）及 getter/setter，验证 `mvn compile` 通过、启动 H2 后 `axle_info` 自动含这些新列
- [x] 1.2 扩展 `RepairGrade` 枚举为「例行/中修/大修/复检」（追加 MAJOR=2、REINSPECT=3），验证编译通过且既有 ROUTINE=0/MIDDLE=1 存储值不变

## 2. 后端：创建接口

- [x] 2.1 新增 `CreateAxleRequest` DTO，字段对齐设计决策 3 并加 `@NotBlank`/`@NotNull` 必填校验注解，验证 `mvn compile` 通过
- [x] 2.2 在 `AxleService` 新增 `create(CreateAxleRequest)`：生成唯一 ID（当日前缀 + 最大序号 +1）、修程/轴位/左右侧/材质白名单校验、「例行检查」归一为「例行」、初始 lifecycleStatus=在役、repairCount=0、写一条 PRODUCED 履历，验证单测或手动调用返回正确落库结果
- [x] 2.3 在 `AxleController` 新增 `POST /api/axles`（`@Valid` 请求体），验证 `curl` 提交完整 JSON 返回 200 且响应含生成的 `axleId`
- [x] 2.4 校验异常分支：缺必填项（如 model 为空）返回 400、非法修程/轴位返回 400，验证错误提示能定位到缺失/非法字段

## 3. 前端：新建轮轴页面

- [x] 3.1 在 `frontend/src/api/axle.js` 新增 `createAxle(payload)` 封装，验证 `npm run dev` 无导入报错
- [x] 3.2 在 `frontend/src/router/index.js` 新增 `/axles/new` 路由（注册在 `/axles/:id` 之前），验证访问 `/axles/new` 能命中新路由而非被 `:id` 吞掉
- [x] 3.3 新增 `frontend/src/views/AxleNewView.vue`：表单字段/枚举选项/必填标记/右侧完整性进度与实时预览对齐 `prototype/axle-new.html`，验证页面渲染与原型一致
- [x] 3.4 实现前端必填校验 + 提交调用 `createAxle` + 成功后跳转详情页，「下一步：检测参数」为占位提示，验证创建成功且跳转正确
- [x] 3.5 在 `AxleListView.vue`「轮轴数字身份」板块头部加「新建轮轴」按钮、`App.vue` 侧边栏加「新建轮轴」导航项，验证点击均跳转 `/axles/new`

## 4. 联调与验收

- [x] 4.1 前后端联调全链路：列表 → 新建 → 提交 → 详情，验证新轮轴出现在列表、详情可查、履历含「生产出厂登记」
- [x] 4.2 对照 `prototype/axle-new.html` 核对字段名、枚举值、必填项、页面结构，并对照 spec 各 Scenario（成功创建/留空可选/缺失必填/ID 唯一/修程四级）逐条验收，验证无术语偏差、无遗漏
