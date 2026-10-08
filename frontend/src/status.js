// 状态显示值 -> CSS 修饰类（与 prototype 视觉一致）
export function statusClass(status) {
  switch (status) {
    case '待复检':
    case '待验收':
      return 'warn'
    case '预警':
      return 'alert'
    case '报废':
      return 'error'
    default:
      // 正常 / 在修 / 已出库
      return 'ok'
  }
}
