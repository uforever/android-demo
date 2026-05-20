package com.example.demo.datastructure.linkedlist;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.demo.databinding.FragmentLinkedListBinding;

public class LinkedListFragment extends Fragment {

    private FragmentLinkedListBinding binding;
    private LRUCache lruCache;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentLinkedListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        lruCache = new LRUCache(3);
        appendLog("LRU缓存已初始化，默认容量: 3");
        appendLog(lruCache.getCacheState());
        appendLog("");

        binding.btnInitCache.setOnClickListener(v -> {
            String capStr = binding.etCapacity.getText().toString().trim();
            if (capStr.isEmpty()) {
                appendLog("请输入缓存容量");
                return;
            }
            int capacity = Integer.parseInt(capStr);
            if (capacity <= 0) {
                appendLog("容量必须大于0");
                return;
            }
            lruCache = new LRUCache(capacity);
            appendLog("LRU缓存已重新初始化，容量: " + capacity);
            appendLog(lruCache.getCacheState());
            appendLog("");
        });

        binding.btnPut.setOnClickListener(v -> {
            String keyStr = binding.etKey.getText().toString().trim();
            String valStr = binding.etValue.getText().toString().trim();
            if (keyStr.isEmpty() || valStr.isEmpty()) {
                appendLog("请输入Key和Value");
                return;
            }
            int key = Integer.parseInt(keyStr);
            int value = Integer.parseInt(valStr);
            appendLog("PUT(" + key + ", " + value + ")");
            lruCache.put(key, value);
            appendLog(lruCache.getCacheState());
            appendLog("");
        });

        binding.btnGet.setOnClickListener(v -> {
            String keyStr = binding.etKey.getText().toString().trim();
            if (keyStr.isEmpty()) {
                appendLog("请输入Key");
                return;
            }
            int key = Integer.parseInt(keyStr);
            appendLog("GET(" + key + ")");
            int result = lruCache.get(key);
            if (result == -1) {
                appendLog("结果: 未找到 key=" + key);
            } else {
                appendLog("结果: value=" + result);
            }
            appendLog(lruCache.getCacheState());
            appendLog("");
        });

        binding.btnClearLog.setOnClickListener(v -> binding.tvLog.setText(""));
    }

    @SuppressLint("SetTextI18n")
    private void appendLog(String message) {
        String current = binding.tvLog.getText().toString();
        if (!current.isEmpty()) {
            current += "\n";
        }
        binding.tvLog.setText(current + message);
        binding.scrollView.post(() -> binding.scrollView.fullScroll(View.FOCUS_DOWN));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
