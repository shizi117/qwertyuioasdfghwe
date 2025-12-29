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

        modelManager = new AIModelManager(this);
        aiService = new AIInferenceService(this);
        rootService = new RootShellService(this);
        fileWatcherService = new FileWatcherService(this);

        initViews();
        requestPermissions();
        checkRootAccess();
        initModelDirectory();
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

        sendButton.setOnClickListener(v -> {
            String input = inputEditText.getText().toString().trim();
            if (!input.isEmpty()) {
                processInput(input);
                inputEditText.setText("");
            }
        });

        fileManagerButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, FileManagerActivity.class);
            startActivity(intent);
        });

        modelManagerButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, ModelManagerActivity.class);
            startActivity(intent);
        });

        clearButton.setOnClickListener(v -> {
            outputTextView.setText("");
        });

        appendToOutput("========================================\n");
        appendToOutput("  Android AI Assistant Started\n");
        appendToOutput("========================================\n\n");
    }

    private void requestPermissions() {
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
            appendToOutput("[System] Root access granted\n\n");
            rootService.initialize();
        } else {
            appendToOutput("[System] No root access detected\n");
            appendToOutput("[System] Some features may not work\n\n");
        }
    }

    private void initModelDirectory() {
        File modelDir = new File(getExternalFilesDir(null), "models");
        if (!modelDir.exists()) {
            modelDir.mkdirs();
        }
        appendToOutput("[System] Model directory: " + modelDir.getAbsolutePath() + "\n\n");
    }

    private void loadDefaultModel() {
        new Thread(() -> {
            File modelDir = new File(getExternalFilesDir(null), "models");
            File[] models = modelDir.listFiles((dir, name) -> name.endsWith(".gguf"));

            if (models != null && models.length > 0) {
                currentModelPath = models[0].getAbsolutePath();
                runOnUiThread(() -> {
                    appendToOutput("[Model] Loaded: " + models[0].getName() + "\n\n");
                });
            } else {
                runOnUiThread(() -> {
                    appendToOutput("[Model] No model found\n");
                    appendToOutput("[Model] Please download a model first\n\n");
                });
            }
        }).start();
    }

    private void processInput(String input) {
        appendToOutput("[You] " + input + "\n");

        if (input.startsWith("/")) {
            processCommand(input);
            return;
        }

        if (currentModelPath == null) {
            appendToOutput("[AI] Please load a model first\n\n");
            return;
        }

        appendToOutput("[AI] Thinking...\n");
        new Thread(() -> {
            try {
                String response = aiService.inference(currentModelPath, input);
                runOnUiThread(() -> {
                    appendToOutput("[AI] " + response + "\n\n");
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    appendToOutput("[Error] " + e.getMessage() + "\n\n");
                });
            }
        }).start();
    }

    private void processCommand(String command) {
        String[] parts = command.split("\\s+");
        String cmd = parts[0].toLowerCase();

        switch (cmd) {
            case "/help":
                appendToOutput("[Help] Available commands:\n");
                appendToOutput("  /help - Show help\n");
                appendToOutput("  /ls [path] - List files\n");
                appendToOutput("  /cat [file] - View file contents\n");
                appendToOutput("  /rm [file] - Delete file\n");
                appendToOutput("  /cp [source] [target] - Copy file\n");
                appendToOutput("  /mv [source] [target] - Move file\n");
                appendToOutput("  /mkdir [path] - Create directory\n");
                appendToOutput("  /model [path] - Load model\n");
                appendToOutput("  /root [command] - Execute root command\n\n");
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
                    appendToOutput("[Error] Please specify file path\n\n");
                }
                break;

            case "/rm":
                if (parts.length > 1) {
                    deleteFileInternal(parts[1]);
                } else {
                    appendToOutput("[Error] Please specify file path\n\n");
                }
                break;

            case "/cp":
                if (parts.length > 2) {
                    copyFile(parts[1], parts[2]);
                } else {
                    appendToOutput("[Error] Usage: /cp [source] [target]\n\n");
                }
                break;

            case "/mv":
                if (parts.length > 2) {
                    moveFile(parts[1], parts[2]);
                } else {
                    appendToOutput("[Error] Usage: /mv [source] [target]\n\n");
                }
                break;

            case "/mkdir":
                if (parts.length > 1) {
                    createDirectory(parts[1]);
                } else {
                    appendToOutput("[Error] Please specify directory path\n\n");
                }
                break;

            case "/model":
                if (parts.length > 1) {
                    loadModel(parts[1]);
                } else {
                    appendToOutput("[Error] Please specify model path\n\n");
                }
                break;

            case "/root":
                if (parts.length > 1) {
                    executeRootCommand(command.substring(6));
                } else {
                    appendToOutput("[Error] Please specify command to execute\n\n");
                }
                break;

            default:
                appendToOutput("[Error] Unknown command: " + cmd + "\n");
                appendToOutput("[Hint] Type /help for help\n\n");
        }
    }

    private void listFiles(String path) {
        new Thread(() -> {
            try {
                String result = FileUtils.listFiles(path);
                runOnUiThread(() -> {
                    appendToOutput("[Files] " + result + "\n\n");
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    appendToOutput("[Error] " + e.getMessage() + "\n\n");
                });
            }
        }).start();
    }

    private void viewFile(String path) {
        new Thread(() -> {
            try {
                String content = FileUtils.readFile(path);
                runOnUiThread(() -> {
                    appendToOutput("[File] " + path + ":\n");
                    appendToOutput(content + "\n\n");
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    appendToOutput("[Error] " + e.getMessage() + "\n\n");
                });
            }
        }).start();
    }

    private void deleteFileInternal(String path) {
        new Thread(() -> {
            try {
                boolean success = FileUtils.deleteFile(path);
                runOnUiThread(() -> {
                    if (success) {
                        appendToOutput("[Success] Deleted: " + path + "\n\n");
                    } else {
                        appendToOutput("[Failed] Delete failed: " + path + "\n\n");
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    appendToOutput("[Error] " + e.getMessage() + "\n\n");
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
                        appendToOutput("[Success] Copied: " + source + " -> " + target + "\n\n");
                    } else {
                        appendToOutput("[Failed] Copy failed\n\n");
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    appendToOutput("[Error] " + e.getMessage() + "\n\n");
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
                        appendToOutput("[Success] Moved: " + source + " -> " + target + "\n\n");
                    } else {
                        appendToOutput("[Failed] Move failed\n\n");
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    appendToOutput("[Error] " + e.getMessage() + "\n\n");
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
                        appendToOutput("[Success] Created directory: " + path + "\n\n");
                    } else {
                        appendToOutput("[Failed] Create directory failed\n\n");
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    appendToOutput("[Error] " + e.getMessage() + "\n\n");
                });
            }
        }).start();
    }

    private void loadModel(String path) {
        new Thread(() -> {
            File modelFile = new File(path);
            if (!modelFile.exists()) {
                runOnUiThread(() -> {
                    appendToOutput("[Error] Model file not found: " + path + "\n\n");
                });
                return;
            }

            currentModelPath = path;
            runOnUiThread(() -> {
                appendToOutput("[Model] Loaded: " + modelFile.getName() + "\n\n");
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
                    appendToOutput("[Error] " + e.getMessage() + "\n\n");
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
                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
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