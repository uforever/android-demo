package com.example.demo.datastructure.graph;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.lsposed.lsparanoid.Obfuscate;

import com.example.demo.databinding.FragmentGraphBinding;

import java.util.Locale;

@Obfuscate
public class GraphFragment extends Fragment {

    private FragmentGraphBinding binding;
    private GraphAlgorithm graph;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentGraphBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        graph = GraphAlgorithm.createSampleGraph();
        appendLog("预置无向图已加载:");
        appendLog(graph.getGraphInfo());
        appendLog("");

        binding.btnBfs.setOnClickListener(v -> {
            String start = binding.etStartNode.getText().toString().trim().toUpperCase(Locale.ROOT);
            if (start.isEmpty()) {
                appendLog("请输入起始节点");
                return;
            }
            appendLog("=== BFS 广度优先遍历 ===");
            appendLog(graph.bfs(start));
            appendLog("");
        });

        binding.btnDfs.setOnClickListener(v -> {
            String start = binding.etStartNode.getText().toString().trim().toUpperCase(Locale.ROOT);
            if (start.isEmpty()) {
                appendLog("请输入起始节点");
                return;
            }
            appendLog("=== DFS 深度优先遍历 ===");
            appendLog(graph.dfs(start));
            appendLog("");
        });

        binding.btnShortestPath.setOnClickListener(v -> {
            String start = binding.etPathStart.getText().toString().trim().toUpperCase(Locale.ROOT);
            String end = binding.etPathEnd.getText().toString().trim().toUpperCase(Locale.ROOT);
            if (start.isEmpty() || end.isEmpty()) {
                appendLog("请输入起点和终点");
                return;
            }
            appendLog("=== 最短路径 ===");
            appendLog(graph.shortestPath(start, end));
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
