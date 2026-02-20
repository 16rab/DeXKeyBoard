# DeX 输入法切换器

一款 Android 应用，旨在借助 Shizuku 权限，在 Samsung DeX 模式下自动或手动切换输入法。

## 功能特性
- **Shizuku 集成：** 无需 Root 权限，通过 Shizuku 执行 ADB 命令管理输入法。
- **DeX 模式检测：** 自动识别设备是否处于 Samsung DeX 桌面模式。
- **输入法管理：** 列出已安装的输入法，并支持一键切换当前默认输入法。
- **自动切换：** 支持在进入 DeX 模式时，自动切换到预设的目标输入法。

## 前置要求
- Android 7.0+ (API 24+)
- 已安装并激活 [Shizuku](https://shizuku.rikka.app/) 应用。
- 开启无线调试（用于激活 Shizuku）。

## 构建指南
1. 使用 Android Studio 打开本项目。
2. 同步 Gradle 依赖。
3. 编译并安装到您的 Samsung 设备。

关于 CI/CD 编译错误排查与环境配置，请参阅 [CI/CD 故障排查与优化指南](CI_TROUBLESHOOTING.md)。
针对最近一次 CI 构建失败的详细分析报告，请参阅 [CI 故障分析与修复报告](CI_ANALYSIS_REPORT.md)。

## 使用说明
1. 打开应用。
2. 当提示时授予 Shizuku 权限。
3. 确认主页显示 "Shizuku: 已授权"。
4. 在列表中点击任意输入法的 "设为默认" 按钮即可立即切换。
5. 前往 "设置" 页面开启 "DeX 模式下自动切换"，并选择您的目标输入法。
6. 连接 DeX 模式，验证输入法是否自动切换成功。

## 许可证
[MIT License](LICENSE)
