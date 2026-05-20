package com.example.demo.datastructure.heap;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

public class TopKFinder {

    public static String findTopK(int[] nums, int k) {
        StringBuilder log = new StringBuilder();

        if (nums == null || nums.length == 0) {
            return "数组为空";
        }
        if (k <= 0 || k > nums.length) {
            return "K值无效，需满足 0 < K ≤ 数组长度(" + nums.length + ")";
        }

        log.append("数组: ");
        for (int n : nums) log.append(n).append(" ");
        log.append("\nK = ").append(k).append("\n\n");

        List<Integer> result = getIntegers(nums, k, log);
        result.sort(Collections.reverseOrder());
        log.append("\nTop ").append(k).append(" 结果: ");
        for (int i = 0; i < result.size(); i++) {
            if (i > 0) log.append(", ");
            log.append(result.get(i));
        }

        return log.toString();
    }

    @NonNull
    private static List<Integer> getIntegers(int[] nums, int k, StringBuilder log) {
        PriorityQueue<Integer> minHeap = new PriorityQueue<>(k);

        for (int i = 0; i < nums.length; i++) {
            int num = nums[i];
            if (minHeap.size() < k) {
                minHeap.offer(num);
                log.append("步骤 ").append(i + 1).append(": 堆未满，加入 ").append(num);
            } else if (num > minHeap.peek()) {
                int removed = minHeap.poll();
                minHeap.offer(num);
                log.append("步骤 ").append(i + 1).append(": ").append(num).append(" > 堆顶 ").append(removed)
                        .append("，替换");
            } else {
                log.append("步骤 ").append(i + 1).append(": ").append(num).append(" ≤ 堆顶 ").append(minHeap.peek())
                        .append("，跳过");
            }
            log.append(" → 堆: ").append(minHeap).append("\n");
        }

        return new ArrayList<>(minHeap);
    }

    public static int[] parseNumbers(String input) {
        String[] parts = input.split("[,，\\s]+");
        int[] nums = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            nums[i] = Integer.parseInt(parts[i].trim());
        }
        return nums;
    }
}
