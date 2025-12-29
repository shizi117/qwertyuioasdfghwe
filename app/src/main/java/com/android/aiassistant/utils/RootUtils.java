package com.android.aiassistant.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;

/**
 * Root 工具类
 */
public class RootUtils {

    /**
     * 检查是否有 root 权限
     */
    public static boolean checkRootAccess() {
        try {
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream outputStream = new DataOutputStream(process.getOutputStream());
            outputStream.writeBytes("exit\n");
            outputStream.flush();
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 执行 root 命令
     */
    public static String executeRootCommand(String command) {
        try {
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream outputStream = new DataOutputStream(process.getOutputStream());
            outputStream.writeBytes(command + "\n");
            outputStream.writeBytes("exit\n");
            outputStream.flush();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line).append("\n");
            }

            process.waitFor();
            return result.toString();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    /**
     * 检查是否已安装 Magisk
     */
    public static boolean isMagiskInstalled() {
        try {
            Process process = Runtime.getRuntime().exec("which magisk");
            process.waitFor();
            return process.exitCode() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取 Magisk 版本
     */
    public static String getMagiskVersion() {
        if (!isMagiskInstalled()) {
            return "Not installed";
        }
        return executeRootCommand("magisk -v").trim();
    }
}