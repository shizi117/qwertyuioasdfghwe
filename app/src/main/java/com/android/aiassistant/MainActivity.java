package com.android.aiassistant;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.android.aiassistant.service.AIInferenceService;
import com.android.aiassistant.service.RootShellService;
import com.android.aiassistant.service.FileWatcherService;
import com.android.aiassistant.utils.FileUtils;
import com.android.aiassistant.utils.RootUtils;
import com.android.aiassistant.utils.AIModelManager;
import java.io.File;

/**
 * 主 Activity - AI 助手界面
 * 支持 root 权限、文件操作、本地 AI 推理
 */
public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSIONS = 1001;
    private static final int REQUEST_MANAGE_STORAGE = 1002;

    private EditText inputEditText;
    private TextView outputTextView;
    private ScrollView scrollView;
    private Button sendButton;
    private Button fileManagerButton;
    private Button modelManagerButton;
    private Button clearButton;

    private AIModelManager modelManager;
    private AIInferenceService aiService;
    private RootShellService rootService;
    private FileWatcherService fileWatcherService;

    private String currentModelPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化服务
        modelManager = new AIModelManager(this);
        aiService = new AIInferenceService(this);
        rootService = new RootShellService(this);
        fileWatcherService = new FileWatcherService(this);

        // 初始化视图
        initViews();

        // 请求权限
        requestPermissions();

        // 检查 root 权限
        checkRootAccess();

        // 初始化模型目录
        initModelDirectory();

        // 加载默认模型
        loadDefaultModel();
    }

    private void initViews() {
        inputEditText = findViewById(R.id.inputEditText);
        outputTextView = findViewById(R.id.outputTextView);
        scrollView = findViewById(R.id.scrollView);
        sendButton = findViewById(R.id.sendButton);
        fileManagerButton = findViewById(R.id.fileManagerButton);
        modelManagerButton = findViewById(R.id.modelManagerButton);
        clearButton = findViewById(R.id.clearButton);

        // 发送按钮
        sendButton.setOnClickListener(v -> {
            String input = inputEditText.getText().toString().trim();
            if (!input.isEmpty()) {
                processInput(input);
                inputEditText.setText("");
            }
        });

        // 文件管理按钮
        fileManagerButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, FileManagerActivity.class);
            startActivity(intent);
        });

        // 模型管理按钮
        modelManagerButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, ModelManagerActivity.class);
            startActivity(intent);
        });

        // 清空按钮
        clearButton.setOnClickListener(v -> {
            outputTextView.setText("");
        });

        appendToOutput("========================================\n");
        appendToOutput("  Android AI 助手已启动\n");
        appendToOutput("========================================\n\n");
    }

    private void requestPermissions() {
        // 检查存储权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(android.net.Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, REQUEST_MANAGE_STORAGE);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                        },
                        REQUEST_PERMISSIONS);
            }
        }
    }

    private void checkRootAccess() {
        boolean hasRoot = RootUtils.checkRootAccess();
        if (hasRoot) {
            appendToOutput("[系统] ✓ Root 权限已获取\n\n");
            rootService.initialize();
        } else {
            appendToOutput("[系统] ✗ 未检测到 Root 权限\n");
            appendToOutput("[系统] 某些功能可能无法使用\n\n");
        }
    }

    private void initModelDirectory() {
        File modelDir = new File(getExternalFilesDir(null), "models");
        if (!modelDir.exists()) {
            modelDir.mkdirs();
        }
        appendToOutput("[系统] 模型目录: " + modelDir.getAbsolutePath() + "\n\n");
    }

    private void loadDefaultModel() {
        new Thread(() -> {
            File modelDir = new File(getExternalFilesDir(null), "models");
            File[] models = modelDir.listFiles((dir, name) -> name.endsWith(".gguf"));

            if (models != null && models.length > 0) {
                currentModelPath = models[0].getAbsolutePath();
                runOnUiThread(() -> {
                    appendToOutput("[模型] 已加载: " + models[0].getName() + "\n\n");
                });
            } else {
                runOnUiThread(() -> {
                    appendToOutput("[模型] 未找到模型文件\n");
                    appendToOutput("[模型] 请点击\"模型管理\"下载模型\n\n");
                });
            }
        }).start();
    }

    private void processInput(String input) {
        appendToOutput("[你] " + input + "\n");

        // 检查是否是命令
        if (input.startsWith("/")) {
            processCommand(input);
            return;
        }

        // 检查是否有模型
        if (currentModelPath == null) {
            appendToOutput("[AI] 请先加载模型\n\n");
            return;
        }

        // AI 推理
        appendToOutput("[AI] 正在思考...\n");
        new Thread(() -> {
            try {
                String response = aiService.inference(currentModelPath, input);
                runOnUiThread(() -> {
                    appendToOutput("[AI] " + response + "\n\n");
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    appendToOutput("[错误] " + e.getMessage() + "\n\n");
                });
            }
        }).start();
    }

    private void processCommand(String command) {
        String[] parts = command.split("\\s+");
        String cmd = parts[0].toLowerCase();

        switch (cmd) {
            case "/help":
                appendToOutput("[帮助] 可用命令:\n");
                appendToOutput("  /help - 显示帮助\n");
                appendToOutput("  /ls [路径] - 列出文件\n");
                appendToOutput("  /cat [文件] - 查看文件内容\n");
                appendToOutput("  /rm [文件] - 删除文件\n");
                appendToOutput("  /cp [源] [目标] - 复制文件\n");
                appendToOutput("  /mv [源] [目标] - 移动文件\n");
                appendToOutput("  /mkdir [路径] - 创建目录\n");
                appendToOutput("  /model [路径] - 加载模型\n");
                appendToOutput("  /root [命令] - 执行 root 命令\n\n");
                break;

            case "/ls":
                if (parts.length > 1) {
                    listFiles(parts[1]);
                } else {
                    listFiles("/sdcard");
                }
                break;

            case "/cat":
                if (parts.length > 1) {
                    viewFile(parts[1]);
                } else {
                    appendToOutput("[错误] 请指定文件路径\n\n");
                }
                break;

            case "/rm":
                if (parts.length > 1) {
                    deleteFile(parts[1]);
                } else {
                    appendToOutput("[错误] 请指定文件路径\n\n");
                }
                break;

            case "/cp":
                if (parts.length > 2) {
                    copyFile(parts[1], parts[2]);
                } else {
                    appendToOutput("[错误] 用法: /cp [源] [目标]\n\n");
                }
                break;

            case "/mv":
                if (parts.length > 2) {
                    moveFile(parts[1], parts[2]);
                } else {
                    appendToOutput("[错误] 用法: /mv [源] [目标]\n\n");
                }
                break;

            case "/mkdir":
                if (parts.length > 1) {
                    createDirectory(parts[1]);
                } else {
                    appendToOutput("[错误] 请指定目录路径\n\n");
                }
                break;

            case "/model":
                if (parts.length > 1) {
                    loadModel(parts[1]);
                } else {
                    appendToOutput("[错误] 请指定模型路径\n\n");
                }
                break;

            case "/root":
                if (parts.length > 1) {
                    executeRootCommand(command.substring(6));
                } else {
                    appendToOutput("[错误] 请指定要执行的命令\n\n");
                }
                break;

            default:
                appendToOutput("[错误] 未知命令: " + cmd + "\n");
                appendToOutput("[提示] 输入 /help 查看帮助\n\n");
        }
    }

    private void listFiles(String path) {
        new Thread(() -> {
            try {
                String result = FileUtils.listFiles(path);
                runOnUiThread(() -> {
                    appendToOutput("[文件] " + result + "\n\n");
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    appendToOutput("[错误] " + e.getMessage() + "\n\n");
                });
            }
        }).start();
    }

    private void viewFile(String path) {
        new Thread(() -> {
            try {
                String content = FileUtils.readFile(path);
                runOnUiThread(() -> {
                    appendToOutput("[文件] " + path + ":\n");
                    appendToOutput(content + "\n\n");
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    appendToOutput("[错误] " + e.getMessage() + "\n\n");
                });
            }
        }).start();
    }

    private void deleteFile(String path) {
        new Thread(() -> {
            try {
                boolean success = FileUtils.deleteFile(path);
                runOnUiThread(() -> {
                    if (success) {
                        appendToOutput("[成功] 已删除: " + path + "\n\n");
                    } else {
                        appendToOutput("[失败] 删除失败: " + path + "\n\n");
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    appendToOutput("[错误] " + e.getMessage() + "\n\n");
                });
            }
        }).start();
    }

    private void copyFile(String source, String target) {
        new Thread(() -> {
            try {
                boolean success = FileUtils.copyFile(source, target);
                runOnUiThread(() -> {
                    if (success) {
                        appendToOutput("[成功] 已复制: " + source + " -> " + target + "\n\n");
                    } else {
                        appendToOutput("[失败] 复制失败\n\n");
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    appendToOutput("[错误] " + e.getMessage() + "\n\n");
                });
            }
        }).start();
    }

    private void moveFile(String source, String target) {
        new Thread(() -> {
            try {
                boolean success = FileUtils.moveFile(source, target);
                runOnUiThread(() -> {
                    if (success) {
                        appendToOutput("[成功] 已移动: " + source + " -> " + target + "\n\n");
                    } else {
                        appendToOutput("[失败] 移动失败\n\n");
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    appendToOutput("[错误] " + e.getMessage() + "\n\n");
                });
            }
        }).start();
    }

    private void createDirectory(String path) {
        new Thread(() -> {
            try {
                boolean success = FileUtils.createDirectory(path);
                runOnUiThread(() -> {
                    if (success) {
                        appendToOutput("[成功] 已创建目录: " + path + "\n\n");
                    } else {
                        appendToOutput("[失败] 创建目录失败\n\n");
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    appendToOutput("[错误] " + e.getMessage() + "\n\n");
                });
            }
        }).start();
    }

    private void loadModel(String path) {
        new Thread(() -> {
            File modelFile = new File(path);
            if (!modelFile.exists()) {
                runOnUiThread(() -> {
                    appendToOutput("[错误] 模型文件不存在: " + path + "\n\n");
                });
                return;
            }

            currentModelPath = path;
            runOnUiThread(() -> {
                appendToOutput("[模型] 已加载: " + modelFile.getName() + "\n\n");
            });
        }).start();
    }

    private void executeRootCommand(String command) {
        new Thread(() -> {
            try {
                String result = rootService.executeCommand(command);
                runOnUiThread(() -> {
                    appendToOutput("[Root] " + result + "\n\n");
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    appendToOutput("[错误] " + e.getMessage() + "\n\n");
                });
            }
        }).start();
    }

    private void appendToOutput(String text) {
        outputTextView.append(text);
        scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "权限已授予", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "权限被拒绝", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (aiService != null) {
            aiService.cleanup();
        }
        if (rootService != null) {
            rootService.cleanup();
        }
    }
}
