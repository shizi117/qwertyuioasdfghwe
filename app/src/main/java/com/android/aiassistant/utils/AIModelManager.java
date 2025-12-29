package com.android.aiassistant.utils;

import android.content.Context;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * AI 模型管理器
 */
public class AIModelManager {

    private Context context;
    private File modelsDirectory;

    public AIModelManager(Context context) {
        this.context = context;
        this.modelsDirectory = new File(context.getExternalFilesDir(null), "models");
        if (!modelsDirectory.exists()) {
            modelsDirectory.mkdirs();
        }
    }

    /**
     * 获取模型目录
     */
    public File getModelsDirectory() {
        return modelsDirectory;
    }

    /**
     * 获取所有模型
     */
    public List<ModelInfo> getAvailableModels() {
        List<ModelInfo> models = new ArrayList<>();
        File[] files = modelsDirectory.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.getName().endsWith(".gguf")) {
                    models.add(new ModelInfo(file));
                }
            }
        }

        return models;
    }

    /**
     * 检查模型是否存在
     */
    public boolean modelExists(String modelName) {
        File modelFile = new File(modelsDirectory, modelName);
        return modelFile.exists();
    }

    /**
     * 删除模型
     */
    public boolean deleteModel(String modelName) {
        File modelFile = new File(modelsDirectory, modelName);
        if (modelFile.exists()) {
            return modelFile.delete();
        }
        return false;
    }

    /**
     * 获取模型信息
     */
    public ModelInfo getModelInfo(String modelName) {
        File modelFile = new File(modelsDirectory, modelName);
        if (modelFile.exists()) {
            return new ModelInfo(modelFile);
        }
        return null;
    }

    /**
     * 模型信息类
     */
    public static class ModelInfo {
        public String name;
        public String path;
        public long size;
        public String formattedSize;
        public String type;

        public ModelInfo(File file) {
            this.name = file.getName();
            this.path = file.getAbsolutePath();
            this.size = file.length();
            this.formattedSize = FileUtils.formatFileSize(size);
            this.type = extractModelType(name);
        }

        private String extractModelType(String filename) {
            String lower = filename.toLowerCase();
            if (lower.contains("qwen")) {
                return "Qwen";
            } else if (lower.contains("llama")) {
                return "Llama";
            } else if (lower.contains("phi")) {
                return "Phi";
            } else if (lower.contains("deepseek")) {
                return "DeepSeek";
            } else if (lower.contains("coder")) {
                return "Code Model";
            } else {
                return "Unknown";
            }
        }
    }
}