package com.android.aiassistant;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileManagerActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private FileAdapter adapter;
    private List<FileItem> fileList;
    private String currentPath;
    private TextView pathText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_manager);

        currentPath = "/";
        pathText = findViewById(R.id.path_text);
        recyclerView = findViewById(R.id.file_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        Button btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> goBack());

        loadFiles(currentPath);
    }

    private void loadFiles(String path) {
        fileList = new ArrayList<>();
        File dir = new File(path);

        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    fileList.add(new FileItem(file.getName(), file.getAbsolutePath(), file.isDirectory()));
                }
            }
        }

        adapter = new FileAdapter(fileList, this::onFileClick);
        recyclerView.setAdapter(adapter);
        pathText.setText(path);
    }

    private void onFileClick(FileItem item) {
        if (item.isDirectory()) {
            currentPath = item.getPath();
            loadFiles(currentPath);
        } else {
            Toast.makeText(this, "Selected: " + item.getName(), Toast.LENGTH_SHORT).show();
        }
    }

    private void goBack() {
        File file = new File(currentPath);
        if (file.getParent() != null) {
            currentPath = file.getParent();
            loadFiles(currentPath);
        }
    }

    static class FileItem {
        private String name;
        private String path;
        private boolean isDirectory;

        public FileItem(String name, String path, boolean isDirectory) {
            this.name = name;
            this.path = path;
            this.isDirectory = isDirectory;
        }

        public String getName() { return name; }
        public String getPath() { return path; }
        public boolean isDirectory() { return isDirectory; }
    }
}
