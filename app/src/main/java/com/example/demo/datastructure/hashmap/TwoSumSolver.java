package com.example.demo.datastructure.hashmap;

import org.lsposed.lsparanoid.Obfuscate;

import java.util.HashMap;
import java.util.Map;

@Obfuscate
public class TwoSumSolver {

    public static String solveByHashMap(int[] nums, int target) {
        StringBuilder log = new StringBuilder();
        log.append("数组: ");
        for (int n : nums) log.append(n).append(" ");
        log.append("\n目标值: ").append(target).append("\n\n");
        log.append("--- 哈希表求解 O(n) ---\n");

        Map<Integer, Integer> map = new HashMap<>();

        for (int i = 0; i < nums.length; i++) {
            int complement = target - nums[i];
            log.append("步骤 ").append(i + 1).append(": nums[").append(i).append("]=").append(nums[i])
                    .append(", 需要补数=").append(complement);

            if (map.containsKey(complement)) {
                int j = map.get(complement);
                log.append("\n  找到! nums[").append(j).append("]=").append(nums[j])
                        .append(" + nums[").append(i).append("]=").append(nums[i])
                        .append(" = ").append(target);
                log.append("\n\n结果: 索引 [").append(j).append(", ").append(i).append("]");
                return log.toString();
            }

            map.put(nums[i], i);
            log.append("\n  未找到，存入 map{").append(nums[i]).append("→").append(i).append("}\n");
        }

        log.append("\n结果: 无解");
        return log.toString();
    }

    public static String solveByBruteForce(int[] nums, int target) {
        StringBuilder log = new StringBuilder();
        log.append("--- 暴力求解 O(n²) ---\n");

        int comparisons = 0;
        for (int i = 0; i < nums.length; i++) {
            for (int j = i + 1; j < nums.length; j++) {
                comparisons++;
                log.append("比较 ").append(comparisons).append(": nums[").append(i).append("]=").append(nums[i])
                        .append(" + nums[").append(j).append("]=").append(nums[j])
                        .append(" = ").append(nums[i] + nums[j]);
                if (nums[i] + nums[j] == target) {
                    log.append(" ✓");
                    log.append("\n\n结果: 索引 [").append(i).append(", ").append(j).append("]");
                    log.append("\n总比较次数: ").append(comparisons);
                    return log.toString();
                }
                log.append(" ✗\n");
            }
        }

        log.append("\n结果: 无解");
        log.append("\n总比较次数: ").append(comparisons);
        return log.toString();
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
