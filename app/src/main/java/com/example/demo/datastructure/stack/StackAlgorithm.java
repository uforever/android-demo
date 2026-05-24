package com.example.demo.datastructure.stack;

import org.lsposed.lsparanoid.Obfuscate;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

@Obfuscate
public class StackAlgorithm {

    public static String bracketMatch(String input) {
        StringBuilder log = new StringBuilder();
        Stack<Character> stack = new Stack<>();

        log.append("输入: ").append(input).append("\n");

        Map<Character, Character> pairs = new HashMap<>();
        pairs.put(')', '(');
        pairs.put(']', '[');
        pairs.put('}', '{');

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '(' || c == '[' || c == '{') {
                stack.push(c);
                log.append("位置 ").append(i).append(": 压入 '").append(c).append("' → 栈: ").append(stack).append("\n");
            } else if (c == ')' || c == ']' || c == '}') {
                if (stack.isEmpty()) {
                    log.append("位置 ").append(i).append(": 遇到 '").append(c).append("' 但栈为空\n");
                    log.append("结果: 不匹配 ✗");
                    return log.toString();
                }
                char top = stack.pop();
                char expected = pairs.get(c);
                if (top != expected) {
                    log.append("位置 ").append(i).append(": '").append(c).append("' 期望 '").append(expected).append("' 但找到 '").append(top).append("'\n");
                    log.append("结果: 不匹配 ✗");
                    return log.toString();
                }
                log.append("位置 ").append(i).append(": 弹出 '").append(top).append("' 匹配 '").append(c).append("' → 栈: ").append(stack).append("\n");
            }
        }

        if (stack.isEmpty()) {
            log.append("结果: 匹配 ✓");
        } else {
            log.append("栈中剩余未匹配: ").append(stack).append("\n");
            log.append("结果: 不匹配 ✗");
        }
        return log.toString();
    }

    public static String evaluateExpression(String expression) {
        StringBuilder log = new StringBuilder();
        log.append("输入: ").append(expression).append("\n");

        try {
            String expr = expression.replaceAll("\\s+", "");
            if (expr.isEmpty()) {
                return "表达式为空";
            }

            Stack<Integer> values = new Stack<>();
            Stack<Character> ops = new Stack<>();
            Map<Character, Integer> precedence = new HashMap<>();
            precedence.put('+', 1);
            precedence.put('-', 1);
            precedence.put('*', 2);
            precedence.put('/', 2);

            int i = 0;
            while (i < expr.length()) {
                char c = expr.charAt(i);
                if (Character.isDigit(c)) {
                    int num = 0;
                    while (i < expr.length() && Character.isDigit(expr.charAt(i))) {
                        num = num * 10 + (expr.charAt(i) - '0');
                        i++;
                    }
                    values.push(num);
                    log.append("压入数字: ").append(num).append("\n");
                    continue;
                } else if (c == '(') {
                    ops.push(c);
                } else if (c == ')') {
                    while (!ops.isEmpty() && ops.peek() != '(') {
                        applyOp(values, ops, log);
                    }
                    if (!ops.isEmpty()) ops.pop();
                } else if (precedence.containsKey(c)) {
                    while (!ops.isEmpty() && ops.peek() != '('
                            && precedence.getOrDefault(ops.peek(), 0) >= precedence.get(c)) {
                        applyOp(values, ops, log);
                    }
                    ops.push(c);
                }
                i++;
            }

            while (!ops.isEmpty()) {
                applyOp(values, ops, log);
            }

            if (values.size() != 1) {
                log.append("结果: 表达式格式错误");
            } else {
                log.append("结果: ").append(values.pop());
            }
        } catch (Exception e) {
            log.append("错误: ").append(e.getMessage());
        }

        return log.toString();
    }

    private static void applyOp(Stack<Integer> values, Stack<Character> ops, StringBuilder log) {
        if (values.size() < 2 || ops.isEmpty()) return;
        char op = ops.pop();
        int b = values.pop();
        int a = values.pop();
        int result;
        switch (op) {
            case '+': result = a + b; break;
            case '-': result = a - b; break;
            case '*': result = a * b; break;
            case '/':
                if (b == 0) throw new ArithmeticException("除零错误");
                result = a / b;
                break;
            default: return;
        }
        values.push(result);
        log.append("计算: ").append(a).append(" ").append(op).append(" ").append(b).append(" = ").append(result).append("\n");
    }
}
