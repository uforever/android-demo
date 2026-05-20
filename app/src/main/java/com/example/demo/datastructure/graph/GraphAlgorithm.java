package com.example.demo.datastructure.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class GraphAlgorithm {

    private final Map<String, List<String>> adjacencyList = new HashMap<>();

    public void addEdge(String from, String to) {
        adjacencyList.computeIfAbsent(from, k -> new ArrayList<>()).add(to);
        adjacencyList.computeIfAbsent(to, k -> new ArrayList<>()).add(from);
    }

    public String getGraphInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("图的邻接表:\n");
        List<String> sortedKeys = new ArrayList<>(adjacencyList.keySet());
        Collections.sort(sortedKeys);
        for (String key : sortedKeys) {
            sb.append("  ").append(key).append(" → ");
            List<String> neighbors = new ArrayList<>(adjacencyList.get(key));
            Collections.sort(neighbors);
            sb.append(neighbors).append("\n");
        }
        return sb.toString();
    }

    public String bfs(String start) {
        StringBuilder log = new StringBuilder();
        if (!adjacencyList.containsKey(start)) {
            return "节点 " + start + " 不存在";
        }

        log.append("BFS 从节点 ").append(start).append(" 开始:\n");
        Set<String> visited = new HashSet<>();
        Queue<String> queue = new LinkedList<>();
        List<String> order = new ArrayList<>();

        queue.offer(start);
        visited.add(start);

        while (!queue.isEmpty()) {
            String node = queue.poll();
            order.add(node);
            log.append("  访问: ").append(node);
            log.append(" | 队列: ").append(queue);
            log.append(" | 已访问: ").append(order).append("\n");

            List<String> neighbors = adjacencyList.getOrDefault(node, Collections.emptyList());
            Collections.sort(neighbors);
            for (String neighbor : neighbors) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    queue.offer(neighbor);
                }
            }
        }

        log.append("\nBFS 遍历顺序: ").append(order);
        return log.toString();
    }

    public String dfs(String start) {
        StringBuilder log = new StringBuilder();
        if (!adjacencyList.containsKey(start)) {
            return "节点 " + start + " 不存在";
        }

        log.append("DFS 从节点 ").append(start).append(" 开始:\n");
        Set<String> visited = new HashSet<>();
        List<String> order = new ArrayList<>();
        dfsHelper(start, visited, order, log, 0);

        log.append("\nDFS 遍历顺序: ").append(order);
        return log.toString();
    }

    private void dfsHelper(String node, Set<String> visited, List<String> order, StringBuilder log, int depth) {
        visited.add(node);
        order.add(node);
        StringBuilder indent = new StringBuilder();
        for (int i = 0; i < depth; i++) indent.append("  ");
        log.append(indent).append("访问: ").append(node).append("\n");

        List<String> neighbors = adjacencyList.getOrDefault(node, Collections.emptyList());
        Collections.sort(neighbors);
        for (String neighbor : neighbors) {
            if (!visited.contains(neighbor)) {
                dfsHelper(neighbor, visited, order, log, depth + 1);
            }
        }
    }

    public String shortestPath(String start, String end) {
        StringBuilder log = new StringBuilder();
        if (!adjacencyList.containsKey(start)) {
            return "起点 " + start + " 不存在";
        }
        if (!adjacencyList.containsKey(end)) {
            return "终点 " + end + " 不存在";
        }

        log.append("最短路径: ").append(start).append(" → ").append(end).append("\n\n");

        Map<String, String> parent = new HashMap<>();
        Set<String> visited = new HashSet<>();
        Queue<String> queue = new LinkedList<>();

        queue.offer(start);
        visited.add(start);
        parent.put(start, null);

        boolean found = false;
        while (!queue.isEmpty()) {
            String node = queue.poll();
            log.append("探索: ").append(node);

            if (node.equals(end)) {
                found = true;
                log.append(" (到达终点!)\n");
                break;
            }
            log.append("\n");

            List<String> neighbors = adjacencyList.getOrDefault(node, Collections.emptyList());
            Collections.sort(neighbors);
            for (String neighbor : neighbors) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    parent.put(neighbor, node);
                    queue.offer(neighbor);
                    log.append("  → 发现 ").append(neighbor).append(" (父节点: ").append(node).append(")\n");
                }
            }
        }

        if (!found) {
            log.append("\n结果: 不可达");
            return log.toString();
        }

        List<String> path = new ArrayList<>();
        String cur = end;
        while (cur != null) {
            path.add(cur);
            cur = parent.get(cur);
        }
        Collections.reverse(path);

        log.append("\n最短路径: ");
        for (int i = 0; i < path.size(); i++) {
            if (i > 0) log.append(" → ");
            log.append(path.get(i));
        }
        log.append("\n路径长度: ").append(path.size() - 1);
        return log.toString();
    }

    public static GraphAlgorithm createSampleGraph() {
        GraphAlgorithm graph = new GraphAlgorithm();
        graph.addEdge("A", "B");
        graph.addEdge("A", "C");
        graph.addEdge("B", "D");
        graph.addEdge("B", "E");
        graph.addEdge("C", "F");
        graph.addEdge("D", "G");
        graph.addEdge("E", "G");
        graph.addEdge("F", "G");
        return graph;
    }
}
