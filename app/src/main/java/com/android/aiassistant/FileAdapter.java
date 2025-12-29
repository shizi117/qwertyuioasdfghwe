package com.android.aiassistant;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.android.aiassistant.FileManagerActivity.FileItem;
import java.util.List;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.ViewHolder> {
    private List<FileItem> fileList;
    private OnFileClickListener listener;

    public interface OnFileClickListener {
        void onFileClick(FileItem item);
    }

    public FileAdapter(List<FileItem> fileList, OnFileClickListener listener) {
        this.fileList = fileList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FileItem item = fileList.get(position);
        holder.textView.setText(item.getName() + (item.isDirectory() ? "/" : ""));
        holder.itemView.setOnClickListener(v -> listener.onFileClick(item));
    }

    @Override
    public int getItemCount() {
        return fileList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public ViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(android.R.id.text1);
        }
    }
}
