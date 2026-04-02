# Android Crash Detector

🔍 轻量级 Android 全局崩溃检测器 - 自动捕获未处理异常并保存日志到外置存储。

## 功能特性

- ✅ 全局捕获未处理异常
- ✅ 自动生成详细崩溃报告（时间戳、线程信息、异常堆栈、设备信息、应用版本）
- ✅ 保存日志到外置存储（/sdcard/Download/crashes/）
- ✅ 零配置，一行代码集成
- ✅ 开源免费（MIT License）

## 快速集成

### 方式 A: 直接复制源码（推荐）

将 `CrashHandler.java` 复制到你的项目中：

```java
// 包名可自定义
package com.example.crashdetector;
```

### 方式 B: 等待 JitPack 发布

```gradle
// 项目级 build.gradle
repositories {
    maven { url 'https://jitpack.io' }
}

// 模块级 build.gradle
dependencies {
    implementation 'com.github.hxcan:android-crash-detector:v1.0.0'
}
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
    ... >
</application>
```

### 3. 添加存储权限（Android 9 及以下）

```xml
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" 
    android:maxSdkVersion="28" />
```

Android 10+ 使用作用域存储，无需额外权限。

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

默认保存在：`/sdcard/Download/crashes/crash_YYYYMMDD_HHmmss.log`

## API 参考

### CrashHandler.init(Context context)

初始化崩溃检测器。

**参数:**
- context: 应用程序上下文

**示例:**
```java
CrashHandler.init(getApplicationContext());
```

## 注意事项

1. **尽早初始化**: 建议在 Application.onCreate() 中初始化
2. **存储权限**: Android 9 及以下需要 WRITE_EXTERNAL_STORAGE 权限
3. **日志清理**: 定期清理旧日志文件，避免占用过多存储空间
4. **生产环境**: 建议在生产环境中将日志上传到服务器

## 构建说明

### 开发环境要求
- Android Studio Arctic Fox 或更高版本
- JDK 11 或更高版本
- Android SDK 21+

### 编译库

```bash
./gradlew :library:assembleRelease
```

## 贡献

欢迎提交 Issue 和 Pull Request！

## 许可证

MIT License - 详见 [LICENSE](LICENSE) 文件

## 作者

- 蔡火胜 ([hxcan](https://github.com/hxcan))

## 更新日志

### v1.0.0 (2026-04-02)
- 🎉 首次发布
- ✅ 实现全局崩溃捕获
- ✅ 自动生成详细崩溃报告
- ✅ 支持保存到外置存储
