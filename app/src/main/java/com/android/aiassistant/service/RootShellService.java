package com.android.aiassistant.service;

import android.content.Context;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;

/**
 * Root Shell 服务
 * 提供执行 root 命令的能力
 */
public class RootShellService {

    private Context context;
    private Process shellProcess;
    private DataOutputStream outputStream;
    private BufferedReader inputStream;
    private BufferedReader errorStream;

    public RootShellService(Context context) {
        this.context = context;
    }

    /**
     * 初始化 root shell
     */
    public void initialize() {
        try {
            shellProcess = Runtime.getRuntime().exec("su");
            outputStream = new DataOutputStream(shellProcess.getOutputStream());
            inputStream = new BufferedReader(new InputStreamReader(shellProcess.getInputStream()));
            errorStream = new BufferedReader(new InputStreamReader(shellProcess.getErrorStream()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 执行 root 命令
     */
    public String executeCommand(String command) throws Exception {
        if (outputStream == null) {
            throw new Exception("Root shell 未初始化");
        }

        StringBuilder result = new StringBuilder();

        try {
            // 发送命令
            outputStream.writeBytes(command + "\n");
            outputStream.flush();

            // 读取输出
            String line;
            while ((line = inputStream.readLine()) != null) {
                result.append(line).append("\n");
            }

            // 读取错误
            while ((line = errorStream.readLine()) != null) {
                result.append("[ERROR] ").append(line).append("\n");
            }

        } catch (Exception e) {
            throw new Exception("执行命令失败: " + e.getMessage());
        }

        return result.toString();
    }

    /**
     * 执行命令并获取返回值
     */
    public CommandResult executeCommandWithResult(String command) {
        CommandResult result = new CommandResult();
        result.command = command;

        try {
            Process process = Runtime.getRuntime().exec(new String[]{"su", "-c", command});

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));
            BufferedReader errorReader = new BufferedReader(
                    new InputStreamReader(process.getErrorStream()));

            StringBuilder output = new StringBuilder();
            StringBuilder error = new StringBuilder();

            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            while ((line = errorReader.readLine()) != null) {
                error.append(line).append("\n");
            }

            result.exitCode = process.waitFor();
            result.output = output.toString();
            result.error = error.toString();
            result.success = result.exitCode == 0;

        } catch (Exception e) {
            result.success = false;
            result.error = e.getMessage();
        }

        return result;
    }

    /**
     * 检查文件是否存在
     */
    public boolean fileExists(String path) {
        CommandResult result = executeCommandWithResult("[ -f '" + path + "' ] && echo 'exists'");
        return result.success && result.output.contains("exists");
    }

    /**
     * 检查目录是否存在
     */
    public boolean directoryExists(String path) {
        CommandResult result = executeCommandWithResult("[ -d '" + path + "' ] && echo 'exists'");
        return result.success && result.output.contains("exists");
    }

    /**
     * 创建目录
     */
    public boolean createDirectory(String path) {
        CommandResult result = executeCommandWithResult("mkdir -p '" + path + "'");
        return result.success;
    }

    /**
     * 删除文件或目录
     */
    public boolean delete(String path) {
        CommandResult result = executeCommandWithResult("rm -rf '" + path + "'");
        return result.success;
    }

    /**
     * 复制文件
     */
    public boolean copy(String source, String target) {
        CommandResult result = executeCommandWithResult("cp -r '" + source + "' '" + target + "'");
        return result.success;
    }

    /**
     * 移动文件
     */
    public boolean move(String source, String target) {
        CommandResult result = executeCommandWithResult("mv '" + source + "' '" + target + "'");
        return result.success;
    }

    /**
     * 读取文件内容
     */
    public String readFile(String path) {
        CommandResult result = executeCommandWithResult("cat '" + path + "'");
        if (result.success) {
            return result.output;
        }
        return null;
    }

    /**
     * 写入文件内容
     */
    public boolean writeFile(String path, String content) {
        CommandResult result = executeCommandWithResult("echo '" + content + "' > '" + path + "'");
        return result.success;
    }

    /**
     * 列出目录内容
     */
    public String listDirectory(String path) {
        CommandResult result = executeCommandWithResult("ls -la '" + path + "'");
        if (result.success) {
            return result.output;
        }
        return null;
    }

    /**
     * 获取文件权限
     */
    public String getFilePermissions(String path) {
        CommandResult result = executeCommandWithResult("ls -ld '" + path + "'");
        if (result.success) {
            return result.output;
        }
        return null;
    }

    /**
     * 修改文件权限
     */
    public boolean chmod(String path, String permissions) {
        CommandResult result = executeCommandWithResult("chmod " + permissions + " '" + path + "'");
        return result.success;
    }

    /**
     * 修改文件所有者
     */
    public boolean chown(String path, String owner) {
        CommandResult result = executeCommandWithResult("chown " + owner + " '" + path + "'");
        return result.success;
    }

    /**
     * 挂载文件系统
     */
    public boolean mount(String source, String target, String type, String options) {
        String cmd = "mount -t " + type;
        if (options != null && !options.isEmpty()) {
            cmd += " -o " + options;
        }
        cmd += " '" + source + "' '" + target + "'";
        CommandResult result = executeCommandWithResult(cmd);
        return result.success;
    }

    /**
     * 卸载文件系统
     */
    public boolean umount(String path) {
        CommandResult result = executeCommandWithResult("umount '" + path + "'");
        return result.success;
    }

    /**
     * 获取进程列表
     */
    public String getProcessList() {
        CommandResult result = executeCommandWithResult("ps -A");
        if (result.success) {
            return result.output;
        }
        return null;
    }

    /**
     * 杀死进程
     */
    public boolean killProcess(int pid) {
        CommandResult result = executeCommandWithResult("kill " + pid);
        return result.success;
    }

    /**
     * 强制杀死进程
     */
    public boolean killProcessForce(int pid) {
        CommandResult result = executeCommandWithResult("kill -9 " + pid);
        return result.success;
    }

    /**
     * 安装 APK
     */
    public boolean installApk(String apkPath) {
        CommandResult result = executeCommandWithResult("pm install -r '" + apkPath + "'");
        return result.success;
    }

    /**
     * 卸载应用
     */
    public boolean uninstallApp(String packageName) {
        CommandResult result = executeCommandWithResult("pm uninstall " + packageName);
        return result.success;
    }

    /**
     * 清理资源
     */
    public void cleanup() {
        try {
            if (outputStream != null) {
                outputStream.writeBytes("exit\n");
                outputStream.flush();
                outputStream.close();
            }
            if (inputStream != null) {
                inputStream.close();
            }
            if (errorStream != null) {
                errorStream.close();
            }
            if (shellProcess != null) {
                shellProcess.destroy();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 命令执行结果
     */
    public static class CommandResult {
        public String command;
        public String output;
        public String error;
        public int exitCode;
        public boolean success;
    }
}
