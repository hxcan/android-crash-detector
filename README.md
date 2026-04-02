# Android Crash Detector

🔍 轻量级 Android 全局崩溃检测器 - 自动捕获未处理异常并保存日志到外置存储。

[![](https://jitpack.io/v/hxcan/android-crash-detector.svg)](https://jitpack.io/#hxcan/android-crash-detector)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![API](https://img.shields.io/badge/API-21%2B-brightgreen.svg)](https://android-arsenal.com/api?level=21)

## 功能特性

- ✅ 全局捕获未处理异常
- ✅ 自动生成详细崩溃报告（时间戳、线程信息、异常堆栈、设备信息、应用版本）
- ✅ 按包名分类保存日志（`/sdcard/Download/crashes/{packageName}/`）
- ✅ 零配置，一行代码集成
- ✅ 开源免费（MIT License）
- ✅ 标准 Android Library 项目结构

## 快速集成

### 方式 A: JitPack 依赖（推荐）

**步骤 1:** 在项目级 `settings.gradle` 中添加 JitPack 仓库：

```gradle
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```

**步骤 2:** 在模块级 `build.gradle` 中添加依赖：

```gradle
dependencies {
    implementation 'com.github.hxcan:android-crash-detector:v1.0.0'
}
```

### 方式 B: 直接复制源码

将 `CrashHandler.java` 复制到你的项目中：

```java
// 路径：library/src/main/java/com/stupidbeauty/crashdetector/CrashHandler.java
// 包名可自定义
package com.example.crashdetector;
```

## 使用方法

### 1. 在 Application 中初始化

```java
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        CrashHandler.init(this);
    }
}
```

### 2. 在 AndroidManifest.xml 中注册 Application

```xml
<application
    android:name=".MyApplication"
    android:icon="@mipmap/ic_launcher"
    android:label="@string/app_name"
    ... >
    <!-- 其他配置 -->
</application>
```

### 3. 添加存储权限（Android 9 及以下）

```xml
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" 
    android:maxSdkVersion="28" />
```

**注意：** Android 10+ (API 29+) 使用作用域存储，无需额外权限。

## 崩溃日志格式

```
========== 崩溃报告 ==========
时间：2026-04-02 15:00:46

【线程信息】
线程名：main
线程 ID: 2

【异常信息】
类型：java.lang.NullPointerException
消息：Attempt to invoke virtual method on a null object reference
堆栈跟踪:
...

【设备信息】
品牌：Xiaomi
型号：MI 8
Android 版本：10
SDK 版本：29
制造商：Xiaomi

【应用信息】
包名：com.example.app
版本名：1.0.0
版本码：1
==============================
```

## 日志文件位置

默认保存在：
```
/sdcard/Download/crashes/{packageName}/crash_{packageName}_{timestamp}.log
```

**示例：**
- 未来姐姐 APK: `/sdcard/Download/crashes/com.stupidbeauty.sisterfuture/crash_com.stupidbeauty.sisterfuture_20260402_150046.log`
- 其他应用：`/sdcard/Download/crashes/com.example.app/crash_com.example.app_20260402_150046.log`

**优势：**
- ✅ 多应用不会冲突
- ✅ 日志文件归属清晰
- ✅ 便于按应用分类管理

## API 参考

### CrashHandler.init(Context context)

初始化崩溃检测器。

**参数:**
- `context`: 应用程序上下文

**示例:**
```java
CrashHandler.init(getApplicationContext());
```

## 高级用法

### 自定义日志目录（可选）

修改 `CrashHandler.java` 中的常量：

```java
private static final String CRASH_LOG_BASE_DIR = "/custom/path/";
```

## 注意事项

1. **尽早初始化**: 建议在 `Application.onCreate()` 中初始化，确保捕获所有崩溃
2. **存储权限**: Android 9 及以下需要 `WRITE_EXTERNAL_STORAGE` 权限
3. **日志清理**: 定期清理旧日志文件，避免占用过多存储空间
   ```java
   // 示例：删除 7 天前的日志
   File logDir = new File("/sdcard/Download/crashes/" + getPackageName());
   long sevenDaysAgo = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000;
   for (File file : logDir.listFiles()) {
       if (file.lastModified() < sevenDaysAgo) {
           file.delete();
       }
   }
   ```
4. **生产环境**: 建议在生产环境中将日志上传到服务器，而非仅保存到本地

## 构建说明

### 开发环境要求
- Android Studio Arctic Fox 或更高版本
- JDK 11 或更高版本
- Android SDK 21+

### 编译库

```bash
./gradlew :library:assembleRelease
```

生成的 AAR 文件位于：`library/build/outputs/aar/library-release.aar`

### 发布到 JitPack

1. 创建 Git 标签：
   ```bash
   git tag v1.0.0
   git push origin v1.0.0
   ```

2. JitPack 会自动构建并发布

3. 验证发布：访问 https://jitpack.io/#hxcan/android-crash-detector

## 项目结构

```
android-crash-detector/
├── build.gradle              # 项目级构建配置
├── settings.gradle           # 包含 library 模块
├── library/                  # 库模块
│   ├── build.gradle          # 库模块构建配置
│   ├── proguard-rules.pro    # ProGuard 规则
│   └── src/main/
│       ├── java/com/stupidbeauty/crashdetector/
│       │   └── CrashHandler.java
│       └── AndroidManifest.xml
├── README.md
├── LICENSE
└── .gitignore
```

## 贡献

欢迎提交 Issue 和 Pull Request！

### 提交 Bug
请提供：
1. 崩溃日志文件内容
2. 设备型号和 Android 版本
3. 复现步骤

### 功能建议
请说明：
1. 使用场景
2. 预期行为
3. 实现建议（可选）

## 许可证

MIT License - 详见 [LICENSE](LICENSE) 文件

## 作者

- 蔡火胜 ([hxcan](https://github.com/hxcan))

## 更新日志

### v1.0.0 (2026-04-02)
- 🎉 首次发布
- ✅ 实现全局崩溃捕获
- ✅ 自动生成详细崩溃报告
- ✅ 支持按包名分类保存到外置存储
- ✅ 标准 Android Library 项目结构
- ✅ JitPack 集成

## 相关链接

- [GitHub 仓库](https://github.com/hxcan/android-crash-detector)
- [JitPack 页面](https://jitpack.io/#hxcan/android-crash-detector)
- [未来姐姐 APK](https://github.com/hxcan/sisterfuture)
