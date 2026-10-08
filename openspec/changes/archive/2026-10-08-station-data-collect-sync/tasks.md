## 1. 后端：实体与枚举

- [x] 1.1 新增 `Station` 枚举（WHEEL_DETECTOR/ULTRASONIC/WEIGHING/HANDHELD，含显示值与「必填检测项」判定逻辑，对齐设计决策 2），验证 `mvn compile` 通过
- [x] 1.2 新增 `DetectionRecord` 实体与 `DetectionRecordRepository`（按 `detectionTime` 倒序查询、按日期聚合统计），验证启动 H2 后 `detection_record` 表自动含设计决策 1 的全部列
- [x] 1.3 扩展 `EventType` 追加 `DETECTED(5, "检测记录")`，验证编译通过且既有 0–4 的存储值不变

## 2. 后端：补录接口

- [x] 2.1 新增 `CreateDetectionRequest` DTO（axleId/station/detectionTime/wear/crack/vibration/temp/remark，字段对齐设计决策 5），验证 `mvn compile` 通过
- [x] 2.2 新增 `DetectionService.create`：校验轮轴存在、工位白名单、按工位必填检测项校验、写 `detection_record`、回写 `Axle`（仅覆盖非 null 指标 + `latestDetection`）、写 `DETECTED` 履历，验证单测或手动调用返回正确落库结果
- [x] 2.3 新增 `DetectionController`：`POST /api/detections`、`GET /api/detections?limit=5`、`GET /api/detections/summary`，验证 `curl` 提交完整 JSON 返回 200 且轮轴详情反映新检测指标
- [x] 2.4 校验异常分支：轮轴不存在返回 404、非法工位返回 400、工位必填检测项缺失返回 400，验证错误提示能定位到缺失/非法字段

## 3. 前端：设备采集页

- [x] 3.1 新增 `frontend/src/api/detection.js`：`submitDetection`/`listDetections`/`getDetectionSummary`，验证 `npm run dev` 无导入报错
- [x] 3.2 在 `frontend/src/router/index.js` 新增 `/device` 路由（name `device-collection`），验证访问 `/device` 能命中新路由
- [x] 3.3 新增 `frontend/src/views/DeviceCollectionView.vue`：工位设备状态（静态列表）/ 数据上传状态 / 采集日志三块面板对齐 `prototype/device.html`，验证页面渲染与原型一致
- [x] 3.4 实现「手动补录」弹窗：轮轴 ID、工位下拉（3 项）、检测时间、四个检测指标、备注，前端完整性校验（缺失弹 toast 列缺失项）+ 提交调 `submitDetection` + 成功后刷新日志与汇总，验证补录成功且日志新增一条「已同步」记录
- [x] 3.5 在 `App.vue` 把「设备采集」由 `nav-disabled` 改为 `router-link` 指向 `/device`，验证点击能跳转且高亮正确

## 4. 联调与验收

- [x] 4.1 前后端联调全链路：设备采集页 → 补录 → 提交 → 轮轴详情，验证详情页立即展示最新检测指标与「检测记录」履历、日志列表新增记录、汇总「待同步」为 0
- [x] 4.2 对照 `prototype/device.html` 核对工位枚举、面板结构与术语，并对照 spec 各 Scenario（成功补录 / 留空可选 / 缺失必填 / 工位必填指标缺失 / 未填任何指标 / 即时同步 / 数据即时可见 / 同步记录 / 上传状态汇总）逐条验收，验证无术语偏差、无遗漏
