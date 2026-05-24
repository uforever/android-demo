package com.example.demo.datastructure.trie;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.lsposed.lsparanoid.Obfuscate;

import com.example.demo.databinding.FragmentTrieBinding;

@Obfuscate
public class TrieFragment extends Fragment {

    private FragmentTrieBinding binding;
    private final AutoCompleteTrie trie = new AutoCompleteTrie();
    private boolean initialized = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentTrieBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (!initialized) {
            initTrie();
            initialized = true;
        }

        binding.btnInsertWord.setOnClickListener(v -> {
            String word = binding.etWord.getText().toString().trim();
            if (word.isEmpty()) {
                appendLog("请输入单词");
                return;
            }
            appendLog("=== 插入单词 ===");
            appendLog(trie.getInsertLog(word));
            appendLog("");
        });

        binding.btnSearchPrefix.setOnClickListener(v -> {
            String prefix = binding.etWord.getText().toString().trim();
            if (prefix.isEmpty()) {
                appendLog("请输入前缀");
                return;
            }
            appendLog("=== 搜索前缀 ===");
            appendLog(trie.getSearchLog(prefix));
            appendLog("");
        });

        binding.btnClearLog.setOnClickListener(v -> binding.tvLog.setText(""));
    }

    private void initTrie() {
        String[] words = {"apple", "application", "apply", "app", "banana",
                "band", "bandwidth", "cat", "catch", "category",
                "dog", "door", "download", "android", "algorithm"};

        appendLog("初始化字典树，预置单词:");
        for (String word : words) {
            trie.insert(word);
            appendLog("  + " + word);
        }
        appendLog("\n字典树构建完成，共 " + words.length + " 个单词\n");
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
