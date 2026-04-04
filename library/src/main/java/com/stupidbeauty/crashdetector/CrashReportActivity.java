package com.stupidbeauty.crashdetector;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 崩溃报告展示 Activity
 * 在独立进程中运行，即使主应用崩溃也能显示
 */
public class CrashReportActivity extends Activity {
    
    private static final String TAG = "CrashReportActivity";
    private static final String EXTRA_CRASH_REPORT = "crash_report";
    
    private TextView crashTextView;
    private Button copyButton;
    private Button shareButton;
    private String crashReport;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Log.i(TAG, "✅ CrashReportActivity 启动");
        
        // 获取崩溃报告内容
        crashReport = getIntent().getStringExtra(EXTRA_CRASH_REPORT);
        if (crashReport == null) {
            crashReport = "崩溃信息为空，无法显示";
            Log.e(TAG, "❌ 崩溃报告内容为空");
        }
        
        // 创建界面
        createUI();
    }
    
    private void createUI() {
        // 主布局
        ScrollView scrollView = new ScrollView(this);
        scrollView.setLayoutParams(new ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT));
        
        TextView titleView = new TextView(this);
        titleView.setText("💥 应用崩溃报告");
        titleView.setTextSize(24);
        titleView.setPadding(32, 32, 32, 16);
        titleView.setTextColor(0xFFD32F2F); // 红色
        
        crashTextView = new TextView(this);
        crashTextView.setText(crashReport);
        crashTextView.setTextSize(12);
        crashTextView.setPadding(32, 16, 32, 32);
        crashTextView.setTextColor(0xFF212121); // 深灰
        crashTextView.setTypeface(android.graphics.Typeface.MONOSPACE);
        crashTextView.setTextIsSelectable(true);
        
        // 按钮布局 - 使用 LinearLayout.LayoutParams
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT);
        
        Button copyBtn = new Button(this);
        copyBtn.setText("📋 复制到剪贴板");
        copyBtn.setLayoutParams(buttonParams);
        copyBtn.setPadding(32, 24, 32, 24);
        copyBtn.setOnClickListener(v -> copyToClipboard());
        
        Button shareBtn = new Button(this);
        shareBtn.setText("📤 分享崩溃报告");
        shareBtn.setLayoutParams(buttonParams);
        shareBtn.setPadding(32, 24, 32, 24);
        shareBtn.setOnClickListener(v -> shareCrashReport());
        
        // 添加到滚动视图
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(titleView);
        layout.addView(crashTextView);
        layout.addView(copyBtn);
        layout.addView(shareBtn);
        
        scrollView.addView(layout);
        setContentView(scrollView);
    }
    
    private void copyToClipboard() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Crash Report", crashReport);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "✅ 崩溃报告已复制到剪贴板", Toast.LENGTH_LONG).show();
        Log.i(TAG, "✅ 崩溃报告已复制到剪贴板");
    }
    
    private void shareCrashReport() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "应用崩溃报告");
        shareIntent.putExtra(Intent.EXTRA_TEXT, crashReport);
        startActivity(Intent.createChooser(shareIntent, "分享崩溃报告"));
        Log.i(TAG, "📤 启动分享界面");
    }
    
    /**
     * 启动崩溃报告 Activity
     * @param context 上下文
     * @param crashReport 崩溃报告内容
     */
    public static void showCrashReport(Context context, String crashReport) {
        try {
            Intent intent = new Intent(context, CrashReportActivity.class);
            intent.putExtra(EXTRA_CRASH_REPORT, crashReport);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(intent);
            Log.i(TAG, "✅ 已启动 CrashReportActivity");
        } catch (Exception e) {
            Log.e(TAG, "❌ 启动 CrashReportActivity 失败：" + e.getMessage(), e);
        }
    }
}
