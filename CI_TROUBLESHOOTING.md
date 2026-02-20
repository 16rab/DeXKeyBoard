# CI/CD 故障排查与优化指南

本文档旨在解决 GitHub Actions 编译过程中可能出现的常见问题，并提供本地复现与修复方案。

## 1. 常见编译错误与修复

### 1.1 `actions/upload-artifact` 版本弃用警告
**错误信息：**
```
Error: This request has been automatically failed because it uses a deprecated version of `actions/upload-artifact: v3`.
```
**原因：**
GitHub Actions 官方已弃用 v3 版本 artifact 操作，要求强制升级至 v4。
**修复方案：**
在 `.github/workflows/android.yml` 中，将所有 `uses: actions/upload-artifact@v3` 替换为 `v4`。
同时建议升级 `checkout` 和 `setup-java` 至 `v4` 版本以保持最佳兼容性。

### 1.2 缺少 `gradlew` 脚本或权限不足
**错误信息：**
```
./gradlew: No such file or directory
```
或
```
./gradlew: Permission denied
```
**原因：**
项目根目录下缺少 Gradle Wrapper 文件 (`gradlew`, `gradle/wrapper/gradle-wrapper.jar` 等)，或者 `gradlew` 脚本没有执行权限。
**修复方案：**
1.  **生成 Wrapper (推荐)：**
    在本地项目根目录运行以下命令生成 Wrapper 文件并提交到 Git：
    ```bash
    gradle wrapper --gradle-version 8.0
    git add gradlew gradlew.bat gradle/wrapper/
    git commit -m "Add Gradle Wrapper"
    ```
2.  **CI 配置兼容：**
    如果不想提交 Wrapper 文件，可在 CI 中直接使用 `gradle` 命令（如当前配置所示），配合 `gradle/actions/setup-gradle` 自动安装指定版本的 Gradle。

### 1.3 依赖版本冲突
**错误信息：**
```
Duplicate class found...
```
或
```
Could not resolve all files for configuration...
```
**原因：**
不同的库依赖了相同库的不同版本。
**排查方法：**
在本地或 CI 中运行：
```bash
gradle app:dependencies
```
查看依赖树，找出冲突的库，并使用 `exclude` 或强制指定版本解决。

## 2. 本地环境复现 CI 错误

为了确保本地环境与 CI 一致，建议按照以下步骤进行验证：

### 2.1 准备环境
确保本地安装了：
*   JDK 17 (与 CI `setup-java` 版本一致)
*   Android SDK (API 33)
*   Gradle 8.0 (如果未生成 wrapper)

### 2.2 运行构建命令
在项目根目录打开终端 (Terminal/PowerShell)，执行与 CI 相同的命令：

```bash
# 1. 清理
gradle clean

# 2. 编译 Debug 包
gradle assembleDebug --stacktrace

# 3. 运行单元测试
gradle testDebugUnitTest

# 4. 运行 Lint 检查
gradle lintDebug
```

如果本地运行成功但 CI 失败，通常是环境差异（如 JDK 版本、SDK 路径）或未提交的文件（如 `local.properties` 中硬编码了路径）导致的。

## 3. CI 性能优化策略

当前 `android.yml` 已包含以下优化：

1.  **Gradle 缓存 (`cache: gradle`)**：
    自动缓存 Gradle 依赖和构建产物，显著缩短后续构建时间。
2.  **超时设置 (`timeout-minutes: 30`)**：
    防止因网络卡顿或死循环导致的长时间挂起，节省 Action 分钟数。
3.  **并发控制 (`concurrency`)**：
    当推送新代码时，自动取消旧的正在运行的构建，避免资源浪费。
4.  **构建报告归档**：
    无论构建成功与否，都会上传 Lint 和 Test 报告，便于下载查看详细错误信息。

## 4. 常用维护命令

*   **查看 Lint 报告**：构建完成后，在 GitHub Actions 页面下载 `lint-report`，解压后用浏览器打开 `lint-results-debug.html`。
*   **查看测试报告**：同上，下载 `test-report`，打开 `index.html`。
