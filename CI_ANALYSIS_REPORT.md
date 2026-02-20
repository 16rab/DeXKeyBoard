# CI 故障分析与修复报告

## 1. 故障概述
最近一次 CI 构建失败，主要报错信息如下：
```
Error: Gradle version 8 does not exists
```
同时伴随有 `actions/upload-artifact` 版本弃用的警告（已在之前的修复中解决）。

## 2. 详细分析

### 2.1 Gradle 版本错误
**日志定位：**
```
Run gradle/actions/setup-gradle@v3 
   with: 
     gradle-version: 8 
...
Error: Gradle version 8 does not exists
```
**原因分析：**
在 `.github/workflows/android.yml` 中，`gradle-version` 被指定为 `8.0`（未加引号）。
YAML 解析器可能将其解析为数字 `8`，或者 GitHub Actions 传递参数时丢失了精度/后缀，导致 `setup-gradle` action 尝试去寻找不存在的 "8" 版本（Gradle 版本通常是 "8.0", "8.1" 等）。
Gradle 官方发布版本列表中没有纯数字 "8" 的版本。

### 2.2 Artifact 上传插件弃用
**日志定位：**
```
Error: This request has been automatically failed because it uses a deprecated version of `actions/upload-artifact: v3`.
```
**原因分析：**
GitHub Actions 官方已弃用 v3 版本，强制要求升级到 v4。

### 2.3 缓存服务警告
**日志定位：**
```
Error: Cache service responded with 400
```
**原因分析：**
可能是由于缓存键配置问题或服务瞬时故障。通常不会导致构建直接失败，但会影响构建速度。建议在 `setup-gradle` 配置明确后观察。

## 3. 修复方案实施

### 3.1 修正 Gradle 版本指定
修改 `.github/workflows/android.yml`，将版本号显式声明为字符串：
```yaml
- name: 设置 Gradle (Setup Gradle)
  uses: gradle/actions/setup-gradle@v3
  with:
    gradle-version: '8.0' # 明确指定为字符串 '8.0'
```

### 3.2 升级 Artifact 插件
已将所有 `actions/upload-artifact` 引用升级为 `v4`：
```yaml
- name: 上传构建产物 (Upload APK)
  uses: actions/upload-artifact@v4
```

### 3.3 验证依赖兼容性
检查 `build.gradle` 配置：
- Android Gradle Plugin: `8.0.0`
- Kotlin: `1.8.0`
- Compile SDK: `33`
- JDK: `17` (CI 环境配置)
**结论：** 所有版本均兼容。

## 4. 本地环境建议与预防措施

### 4.1 生成 Gradle Wrapper (推荐)
目前项目根目录缺少 `gradlew` 脚本，导致必须依赖环境中的 Gradle。
建议在本地开发环境（如安装了 Gradle 的机器）运行以下命令生成 Wrapper，并提交到仓库：
```bash
gradle wrapper --gradle-version 8.0
```
这将确保所有开发者和 CI 环境使用完全一致的 Gradle 版本。

### 4.2 本地验证步骤
1. **安装 Gradle 8.0+**：确保本地环境变量中有 `gradle` 命令。
2. **运行构建**：
   ```bash
   gradle assembleDebug --stacktrace
   ```
3. **运行测试**：
   ```bash
   gradle testDebugUnitTest
   ```

### 4.3 CI 稳定性增强
- **超时设置**：已在 job 级别添加 `timeout-minutes: 30`，防止任务挂死。
- **并发控制**：已启用 `concurrency` 策略，自动取消旧的构建请求，节省资源。
- **Lint 容错**：`gradle lintDebug --continue` 确保非致命 Lint 错误不中断流程（可根据需要调整）。

## 5. 结论
通过修正 `gradle-version` 格式并升级相关 Actions 插件，CI 构建流程应能恢复正常。建议尽快生成并提交 `gradlew` 文件以进一步增强构建的一致性。
