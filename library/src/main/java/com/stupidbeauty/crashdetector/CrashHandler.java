package com.stupidbeauty.crashdetector;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 全局崩溃处理器（增强版）
 * 捕获未处理的异常，将崩溃信息保存到：
 * 1. 应用私有目录（不需要权限，始终可用）
 * 2. 外置存储（如果权限允许）
 * 3. 启动 CrashReportActivity 显示崩溃信息（独立进程）
 * 
 * @author 蔡火胜 (hxcan)
 * @version 2.0.0
 */
public class CrashHandler implements Thread.UncaughtExceptionHandler {
    
    private static final String TAG = "CrashDetector";
    private static final String EXTERNAL_CRASH_LOG_BASE_DIR = "/sdcard/Download/crashes/";
    
    private final Context context;
    private final Thread.UncaughtExceptionHandler defaultHandler;
    private final String packageName;
    
    /**
     * 构造函数
     * @param context 应用程序上下文
     */
    public CrashHandler(Context context) {
        this.context = context.getApplicationContext();
        this.packageName = context.getPackageName();
        this.defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
    }
    
    /**
     * 初始化崩溃处理器
     * 应在 Application.onCreate() 中调用
     * 
     * @param context 应用程序上下文
     */
    public static void init(Context context) {
        CrashHandler handler = new CrashHandler(context);
        Thread.setDefaultUncaughtExceptionHandler(handler);
        Log.i(TAG, "✅ Android Crash Detector v2.0.0 已初始化 (包名：" + context.getPackageName() + ")");
        
        // 确保外置存储日志目录存在
        String externalLogDir = getExternalLogDir(context.getPackageName());
        File externalDir = new File(externalLogDir);
        if (!externalDir.exists()) {
            boolean created = externalDir.mkdirs();
            Log.i(TAG, "创建外置日志目录：" + externalLogDir + (created ? " 成功" : " 失败"));
        }
        
        // 确保应用私有日志目录存在
        String privateLogDir = getPrivateLogDir(context);
        File privateDir = new File(privateLogDir);
        if (!privateDir.exists()) {
            boolean created = privateDir.mkdirs();
            Log.i(TAG, "创建私有日志目录：" + privateLogDir + (created ? " 成功" : " 失败"));
        }
    }
    
    /**
     * 获取外置存储的日志目录路径
     */
    private static String getExternalLogDir(String packageName) {
        return EXTERNAL_CRASH_LOG_BASE_DIR + packageName + "/";
    }
    
    /**
     * 获取应用私有目录的日志路径
     */
    private static String getPrivateLogDir(Context context) {
        return context.getFilesDir().getAbsolutePath() + "/crashes/";
    }
    
    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        Log.e(TAG, "❗ 捕获到未处理异常：" + throwable.getMessage());
        
        // 生成崩溃报告
        String crashReport = generateCrashReport(thread, throwable);
        
        // 1. 写入应用私有目录（不需要权限，始终成功）
        saveToPrivateStorage(crashReport);
        
        // 2. 尝试写入外置存储（如果权限允许）
        saveToExternalStorage(crashReport);
        
        // 3. 启动 CrashReportActivity 显示崩溃信息（独立进程）
        showCrashReportUI(crashReport);
        
        // 交给默认处理器处理（显示系统崩溃对话框）
        if (defaultHandler != null) {
            try {
                defaultHandler.uncaughtException(thread, throwable);
            } catch (Exception e) {
                Log.e(TAG, "默认处理器执行失败：" + e.getMessage());
            }
        }
        
        // 等待 UI 显示（可选，防止立即退出）
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Log.e(TAG, "等待中断：" + e.getMessage());
        }
    }
    
    /**
     * 生成崩溃报告
     */
    private String generateCrashReport(Thread thread, Throwable throwable) {
        StringBuilder report = new StringBuilder();
        
        // 时间戳
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        String timestamp = sdf.format(new Date());
        report.append("========== 崩溃报告 ==========\n");
        report.append("时间：").append(timestamp).append("\n\n");
        
        // 线程信息
        report.append("【线程信息】\n");
        report.append("线程名：").append(thread.getName()).append("\n");
        report.append("线程 ID: ").append(thread.getId()).append("\n\n");
        
        // 异常信息
        report.append("【异常信息】\n");
        report.append("类型：").append(throwable.getClass().getName()).append("\n");
        report.append("消息：").append(throwable.getMessage() != null ? throwable.getMessage() : "null").append("\n");
        report.append("堆栈跟踪:\n");
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        report.append(sw.toString());
        pw.close();
        
        // 设备信息
        report.append("\n【设备信息】\n");
        report.append("品牌：").append(Build.BRAND).append("\n");
        report.append("型号：").append(Build.MODEL).append("\n");
        report.append("Android 版本：").append(Build.VERSION.RELEASE).append("\n");
        report.append("SDK 版本：").append(Build.VERSION.SDK_INT).append("\n");
        report.append("制造商：").append(Build.MANUFACTURER).append("\n");
        
        // 应用信息
        report.append("\n【应用信息】\n");
        report.append("包名：").append(packageName).append("\n");
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(packageName, 0);
            report.append("版本名：").append(packageInfo.versionName).append("\n");
            report.append("版本码：").append(packageInfo.versionCode).append("\n");
        } catch (Exception e) {
            report.append("获取版本信息失败：").append(e.getMessage()).append("\n");
        }
        
        report.append("\n==============================\n");
        
        return report.toString();
    }
    
    /**
     * 保存崩溃日志到应用私有目录（不需要权限）
     */
    private void saveToPrivateStorage(String crashReport) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA);
        String timestamp = sdf.format(new Date());
        String fileName = "crash_" + packageName + "_" + timestamp + ".log";
        String logDir = getPrivateLogDir(context);
        File logFile = new File(logDir + fileName);
        
        try (FileWriter writer = new FileWriter(logFile)) {
            writer.write(crashReport);
            Log.i(TAG, "✅ 崩溃日志已保存到私有目录：" + logFile.getAbsolutePath());
        } catch (Exception e) {
            Log.e(TAG, "❌ 保存到私有目录失败：" + e.getMessage());
        }
    }
    
    /**
     * 保存崩溃日志到外置存储（需要权限检查）
     */
    private void saveToExternalStorage(String crashReport) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA);
        String timestamp = sdf.format(new Date());
        String fileName = "crash_" + packageName + "_" + timestamp + ".log";
        String logDir = getExternalLogDir(packageName);
        File logFile = new File(logDir + fileName);
        
        try {
            // 检查外置存储是否可用
            if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                Log.w(TAG, "⚠️ 外置存储不可用，跳过外置存储写入");
                return;
            }
            
            // 尝试创建目录
            File dir = new File(logDir);
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    Log.w(TAG, "⚠️ 无法创建外置日志目录，可能缺少权限");
                    return;
                }
            }
            
            // 写入文件
            try (FileWriter writer = new FileWriter(logFile)) {
                writer.write(crashReport);
                Log.i(TAG, "✅ 崩溃日志已保存到外置存储：" + logFile.getAbsolutePath());
            }
        } catch (Exception e) {
            Log.w(TAG, "⚠️ 保存到外置存储失败（可能是权限问题）: " + e.getMessage());
        }
    }
    
    /**
     * 启动 CrashReportActivity 显示崩溃信息
     */
    private void showCrashReportUI(String crashReport) {
        try {
            CrashReportActivity.showCrashReport(context, crashReport);
            Log.i(TAG, "✅ 已启动崩溃报告界面");
        } catch (Exception e) {
            Log.e(TAG, "❌ 启动崩溃报告界面失败：" + e.getMessage(), e);
        }
    }
}
