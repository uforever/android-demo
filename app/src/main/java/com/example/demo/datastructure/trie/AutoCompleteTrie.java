package com.example.demo.datastructure.trie;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AutoCompleteTrie {

    private static class TrieNode {
        TrieNode[] children = new TrieNode[26];
        boolean isEnd;
    }

    private final TrieNode root = new TrieNode();

    public void insert(String word) {
        TrieNode node = root;
        for (char c : word.toLowerCase(Locale.ROOT).toCharArray()) {
            if (c < 'a' || c > 'z') continue;
            int idx = c - 'a';
            if (node.children[idx] == null) {
                node.children[idx] = new TrieNode();
            }
            node = node.children[idx];
        }
        node.isEnd = true;
    }

    public List<String> searchByPrefix(String prefix) {
        List<String> results = new ArrayList<>();
        TrieNode node = root;
        for (char c : prefix.toLowerCase(Locale.ROOT).toCharArray()) {
            if (c < 'a' || c > 'z') return results;
            int idx = c - 'a';
            if (node.children[idx] == null) return results;
            node = node.children[idx];
        }
        collectWords(node, prefix.toLowerCase(Locale.ROOT), results);
        return results;
    }

    private void collectWords(TrieNode node, String prefix, List<String> results) {
        if (node.isEnd) {
            results.add(prefix);
        }
        for (int i = 0; i < 26; i++) {
            if (node.children[i] != null) {
                collectWords(node.children[i], prefix + (char) ('a' + i), results);
            }
        }
    }

    public String getInsertLog(String word) {
        StringBuilder log = new StringBuilder();
        log.append("插入: ").append(word).append("\n");
        TrieNode node = root;
        StringBuilder path = new StringBuilder();
        for (char c : word.toLowerCase(Locale.ROOT).toCharArray()) {
            if (c < 'a' || c > 'z') continue;
            int idx = c - 'a';
            path.append(c);
            if (node.children[idx] == null) {
                log.append("  创建节点: '").append(c).append("' (路径: ").append(path).append(")\n");
                node.children[idx] = new TrieNode();
            } else {
                log.append("  已有节点: '").append(c).append("' (路径: ").append(path).append(")\n");
            }
            node = node.children[idx];
        }
        node.isEnd = true;
        log.append("  标记单词结束\n");
        return log.toString();
    }

    public String getSearchLog(String prefix) {
        StringBuilder log = new StringBuilder();
        log.append("搜索前缀: ").append(prefix).append("\n");
        TrieNode node = root;
        StringBuilder path = new StringBuilder();
        for (char c : prefix.toLowerCase(Locale.ROOT).toCharArray()) {
            if (c < 'a' || c > 'z') {
                log.append("  非法字符: '").append(c).append("'\n");
                log.append("结果: 无匹配\n");
                return log.toString();
            }
            int idx = c - 'a';
            path.append(c);
            if (node.children[idx] == null) {
                log.append("  未找到节点: '").append(c).append("' (路径: ").append(path).append(")\n");
                log.append("结果: 无匹配\n");
                return log.toString();
            }
            log.append("  匹配节点: '").append(c).append("' (路径: ").append(path).append(")\n");
            node = node.children[idx];
        }
        List<String> results = searchByPrefix(prefix);
        if (results.isEmpty()) {
            log.append("结果: 无匹配单词\n");
        } else {
            log.append("结果: 找到 ").append(results.size()).append(" 个匹配\n");
            for (String w : results) {
                log.append("  → ").append(w).append("\n");
            }
        }
        return log.toString();
    }
}
