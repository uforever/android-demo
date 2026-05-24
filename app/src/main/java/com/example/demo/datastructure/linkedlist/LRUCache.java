package com.example.demo.datastructure.linkedlist;

import org.lsposed.lsparanoid.Obfuscate;

import java.util.HashMap;
import java.util.Map;

@Obfuscate
public class LRUCache {

    private final int capacity;
    private final Map<Integer, Node> cache;
    private final Node head;
    private final Node tail;
    private int size;

    private static class Node {
        int key;
        int value;
        Node prev;
        Node next;

        Node(int key, int value) {
            this.key = key;
            this.value = value;
        }
    }

    public LRUCache(int capacity) {
        this.capacity = capacity;
        this.cache = new HashMap<>();
        this.head = new Node(0, 0);
        this.tail = new Node(0, 0);
        head.next = tail;
        tail.prev = head;
        this.size = 0;
    }

    public int get(int key) {
        Node node = cache.get(key);
        if (node == null) {
            return -1;
        }
        moveToHead(node);
        return node.value;
    }

    public void put(int key, int value) {
        Node node = cache.get(key);
        if (node != null) {
            node.value = value;
            moveToHead(node);
            return;
        }
        Node newNode = new Node(key, value);
        cache.put(key, newNode);
        addToHead(newNode);
        size++;
        if (size > capacity) {
            Node removed = removeTail();
            cache.remove(removed.key);
            size--;
        }
    }

    public String getCacheState() {
        StringBuilder sb = new StringBuilder();
        sb.append("缓存容量: ").append(capacity)
                .append(" | 当前大小: ").append(size).append("\n");
        sb.append("访问顺序 (最近→最久): ");
        Node cur = head.next;
        boolean first = true;
        while (cur != tail) {
            if (!first) sb.append(" → ");
            sb.append("[").append(cur.key).append(":").append(cur.value).append("]");
            first = false;
            cur = cur.next;
        }
        if (first) sb.append("(空)");
        return sb.toString();
    }

    private void addToHead(Node node) {
        node.prev = head;
        node.next = head.next;
        head.next.prev = node;
        head.next = node;
    }

    private void removeNode(Node node) {
        node.prev.next = node.next;
        node.next.prev = node.prev;
    }

    private void moveToHead(Node node) {
        removeNode(node);
        addToHead(node);
    }

    private Node removeTail() {
        Node node = tail.prev;
        removeNode(node);
        return node;
    }
}
