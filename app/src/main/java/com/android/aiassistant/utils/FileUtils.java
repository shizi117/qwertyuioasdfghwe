package com.android.aiassistant.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.nio.channels.FileChannel;

/**
 * 文件工具类
 */
public class FileUtils {

    /**
     * 列出目录内容
     */
    public static String listFiles(String path) throws Exception {
        File dir = new File(path);
        if (!dir.exists()) {
            throw new Exception("目录不存在: " + path);
        }

        if (!dir.isDirectory()) {
            throw new Exception("不是目录: " + path);
        }

        StringBuilder result = new StringBuilder();
        result.append("目录: ").append(path).append("\n");
        result.append("========================================\n");

        File[] files = dir.listFiles();
        if (files == null || files.length == 0) {
            result.append("(空目录)\n");
        } else {
            for (File file : files) {
                String type = file.isDirectory() ? "[DIR]" : "[FILE]";
                String size = file.isDirectory() ? "" : formatFileSize(file.length());
                result.append(String.format("%s %-40s %s\n", type, file.getName(), size));
            }
        }

        return result.toString();
    }

    /**
     * 读取文件内容
     */
    public static String readFile(String path) throws Exception {
        File file = new File(path);
        if (!file.exists()) {
            throw new Exception("文件不存在: " + path);
        }

        if (!file.isFile()) {
            throw new Exception("不是文件: " + path);
        }

        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }

        return content.toString();
    }

    /**
     * 写入文件内容
     */
    public static boolean writeFile(String path, String content) throws Exception {
        File file = new File(path);
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            outputStream.write(content.getBytes());
            return true;
        }
    }

    /**
     * 删除文件或目录
     */
    public static boolean deleteFile(String path) throws Exception {
        File file = new File(path);
        if (!file.exists()) {
            return false;
        }

        if (file.isDirectory()) {
            return deleteDirectory(file);
        } else {
            return file.delete();
        }
    }

    /**
     * 递归删除目录
     */
    private static boolean deleteDirectory(File dir) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        return dir.delete();
    }

    /**
     * 复制文件
     */
    public static boolean copyFile(String sourcePath, String targetPath) throws Exception {
        File source = new File(sourcePath);
        File target = new File(targetPath);

        if (!source.exists()) {
            throw new Exception("源文件不存在: " + sourcePath);
        }

        if (source.isDirectory()) {
            return copyDirectory(source, target);
        }

        try (FileInputStream fis = new FileInputStream(source);
             FileOutputStream fos = new FileOutputStream(target);
             FileChannel sourceChannel = fis.getChannel();
             FileChannel destChannel = fos.getChannel()) {

            destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
            return true;
        }
    }

    /**
     * 复制目录
     */
    private static boolean copyDirectory(File source, File target) throws Exception {
        if (!target.exists()) {
            target.mkdirs();
        }

        File[] files = source.listFiles();
        if (files != null) {
            for (File file : files) {
                File targetFile = new File(target, file.getName());
                if (file.isDirectory()) {
                    copyDirectory(file, targetFile);
                } else {
                    copyFile(file.getAbsolutePath(), targetFile.getAbsolutePath());
                }
            }
        }

        return true;
    }

    /**
     * 移动文件
     */
    public static boolean moveFile(String sourcePath, String targetPath) throws Exception {
        if (copyFile(sourcePath, targetPath)) {
            return deleteFile(sourcePath);
        }
        return false;
    }

    /**
     * 创建目录
     */
    public static boolean createDirectory(String path) throws Exception {
        File dir = new File(path);
        if (dir.exists()) {
            return dir.isDirectory();
        }
        return dir.mkdirs();
    }

    /**
     * 格式化文件大小
     */
    public static String formatFileSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.2f KB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", size / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", size / (1024.0 * 1024 * 1024));
        }
    }

    /**
     * 获取文件扩展名
     */
    public static String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot > 0 && lastDot < filename.length() - 1) {
            return filename.substring(lastDot + 1).toLowerCase();
        }
        return "";
    }

    /**
     * 检查是否是图片文件
     */
    public static boolean isImageFile(String filename) {
        String ext = getFileExtension(filename);
        return ext.equals("jpg") || ext.equals("jpeg") || ext.equals("png") ||
               ext.equals("gif") || ext.equals("bmp") || ext.equals("webp");
    }

    /**
     * 检查是否是文本文件
     */
    public static boolean isTextFile(String filename) {
        String ext = getFileExtension(filename);
        return ext.equals("txt") || ext.equals("md") || ext.equals("json") ||
               ext.equals("xml") || ext.equals("html") || ext.equals("css") ||
               ext.equals("js") || ext.equals("java") || ext.equals("py") ||
               ext.equals("cpp") || ext.equals("c") || ext.equals("h");
    }
}
