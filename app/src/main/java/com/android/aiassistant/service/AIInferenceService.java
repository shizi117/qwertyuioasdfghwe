package com.android.aiassistant.service;

import android.content.Context;
import android.util.Log;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * AI 推理服务
 * 使用 llama.cpp 进行本地 AI 推理
 */
public class AIInferenceService {

    private static final String TAG = "AIInferenceService";

    private Context context;
    private Process inferenceProcess;
    private boolean isRunning;

    // 推理参数
    private int contextLength = 2048;
    private int batchSize = 512;
    private float temperature = 0.7f;
    private float topP = 0.9f;
    private int maxTokens = 512;

    public AIInferenceService(Context context) {
        this.context = context;
    }

    /**
     * 执行 AI 推理
     */
    public String inference(String modelPath, String prompt) throws Exception {
        File modelFile = new File(modelPath);
        if (!modelFile.exists()) {
            throw new Exception("模型文件不存在: " + modelPath);
        }

        // 构建 llama.cpp 命令
        List<String> command = new ArrayList<>();
        command.add(getLlamaCppPath());
        command.add("-m");
        command.add(modelPath);
        command.add("-p");
        command.add(prompt);
        command.add("-n");
        command.add(String.valueOf(maxTokens));
        command.add("--ctx-size");
        command.add(String.valueOf(contextLength));
        command.add("--temp");
        command.add(String.valueOf(temperature));
        command.add("--top-p");
        command.add(String.valueOf(topP));
        command.add("-b");
        command.add(String.valueOf(batchSize));

        // 执行命令
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);

        inferenceProcess = processBuilder.start();
        isRunning = true;

        // 读取输出
        StringBuilder output = new StringBuilder();
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(inferenceProcess.getInputStream()));

        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
        }

        // 等待进程结束
        int exitCode = inferenceProcess.waitFor();
        isRunning = false;

        if (exitCode != 0) {
            throw new Exception("推理失败，退出码: " + exitCode);
        }

        return output.toString().trim();
    }

    /**
     * 流式推理
     */
    public void streamInference(String modelPath, String prompt, InferenceCallback callback) {
        new Thread(() -> {
            try {
                File modelFile = new File(modelPath);
                if (!modelFile.exists()) {
                    callback.onError("模型文件不存在: " + modelPath);
                    return;
                }

                List<String> command = new ArrayList<>();
                command.add(getLlamaCppPath());
                command.add("-m");
                command.add(modelPath);
                command.add("-p");
                command.add(prompt);
                command.add("-n");
                command.add(String.valueOf(maxTokens));
                command.add("--ctx-size");
                command.add(String.valueOf(contextLength));
                command.add("--temp");
                command.add(String.valueOf(temperature));
                command.add("--top-p");
                command.add(String.valueOf(topP));
                command.add("-b");
                command.add(String.valueOf(batchSize));

                ProcessBuilder processBuilder = new ProcessBuilder(command);
                processBuilder.redirectErrorStream(true);

                inferenceProcess = processBuilder.start();
                isRunning = true;

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(inferenceProcess.getInputStream()));

                String line;
                while ((line = reader.readLine()) != null) {
                    if (!isRunning) {
                        break;
                    }
                    callback.onToken(line);
                }

                int exitCode = inferenceProcess.waitFor();
                isRunning = false;

                if (exitCode != 0) {
                    callback.onError("推理失败，退出码: " + exitCode);
                } else {
                    callback.onComplete();
                }

            } catch (Exception e) {
                isRunning = false;
                callback.onError(e.getMessage());
            }
        }).start();
    }

    /**
     * 停止推理
     */
    public void stopInference() {
        if (inferenceProcess != null && isRunning) {
            isRunning = false;
            inferenceProcess.destroy();
        }
    }

    /**
     * 设置推理参数
     */
    public void setParameters(int contextLength, int batchSize, float temperature, float topP, int maxTokens) {
        this.contextLength = contextLength;
        this.batchSize = batchSize;
        this.temperature = temperature;
        this.topP = topP;
        this.maxTokens = maxTokens;
    }

    /**
     * 获取 llama.cpp 路径
     */
    private String getLlamaCppPath() {
        // 检查是否存在预编译的 llama.cpp
        File llamaCpp = new File(context.getExternalFilesDir(null), "llama.cpp/main");
        if (llamaCpp.exists()) {
            return llamaCpp.getAbsolutePath();
        }

        // 检查系统路径
        if (new File("/data/local/tmp/llama.cpp").exists()) {
            return "/data/local/tmp/llama.cpp";
        }

        // 返回默认路径
        return new File(context.getExternalFilesDir(null), "llama.cpp/main").getAbsolutePath();
    }

    /**
     * 检查 llama.cpp 是否可用
     */
    public boolean isLlamaCppAvailable() {
        return new File(getLlamaCppPath()).exists();
    }

    /**
     * 清理资源
     */
    public void cleanup() {
        stopInference();
    }

    /**
     * 推理回调接口
     */
    public interface InferenceCallback {
        void onToken(String token);
        void onComplete();
        void onError(String error);
    }
}