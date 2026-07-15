package com.blog.util;

import java.util.*;

/**
 * AC自动机（Aho-Corasick算法）实现
 * 用于高效的多模式字符串匹配，适用于敏感词检测
 */
public class AhoCorasick {

    /**
     * AC自动机节点
     */
    private static class Node {
        // 子节点映射
        Map<Character, Node> children = new HashMap<>();
        // 失败指针
        Node fail;
        // 是否为模式串结尾
        boolean isEnd = false;
        // 匹配到的模式串（敏感词）
        String pattern;
    }

    private final Node root;

    public AhoCorasick() {
        this.root = new Node();
    }

    /**
     * 添加模式串（敏感词）到Trie树
     * 
     * @param pattern 模式串
     */
    public void addPattern(String pattern) {
        if (pattern == null || pattern.isEmpty()) {
            return;
        }

        Node current = root;
        for (char c : pattern.toLowerCase().toCharArray()) {
            current = current.children.computeIfAbsent(c, k -> new Node());
        }
        current.isEnd = true;
        current.pattern = pattern;
    }

    /**
     * 构建失败指针（KMP的失败函数）
     * 使用BFS遍历Trie树
     */
    public void build() {
        Queue<Node> queue = new LinkedList<>();
        
        // 第一层节点的失败指针指向根节点
        for (Node child : root.children.values()) {
            child.fail = root;
            queue.offer(child);
        }

        // BFS构建失败指针
        while (!queue.isEmpty()) {
            Node current = queue.poll();

            for (Map.Entry<Character, Node> entry : current.children.entrySet()) {
                char c = entry.getKey();
                Node child = entry.getValue();

                // 查找失败指针
                Node failNode = current.fail;
                while (failNode != null && !failNode.children.containsKey(c)) {
                    failNode = failNode.fail;
                }

                if (failNode == null) {
                    child.fail = root;
                } else {
                    child.fail = failNode.children.get(c);
                }

                queue.offer(child);
            }
        }
    }

    /**
     * 搜索文本中的所有匹配模式串
     * 
     * @param text 待搜索文本
     * @return 匹配到的模式串列表
     */
    public List<String> search(String text) {
        List<String> matches = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return matches;
        }

        Node current = root;
        String lowerText = text.toLowerCase();

        for (int i = 0; i < lowerText.length(); i++) {
            char c = lowerText.charAt(i);

            // 跳过空格和特殊字符（可选，根据需求调整）
            if (!Character.isLetterOrDigit(c) && !isChinese(c)) {
                continue;
            }

            // 沿着失败指针查找匹配
            while (current != root && !current.children.containsKey(c)) {
                current = current.fail;
            }

            if (current.children.containsKey(c)) {
                current = current.children.get(c);
            }

            // 检查当前节点及其失败指针链上的所有匹配
            Node temp = current;
            while (temp != root) {
                if (temp.isEnd) {
                    matches.add(temp.pattern);
                }
                temp = temp.fail;
            }
        }

        return matches;
    }

    /**
     * 检查文本是否包含任何模式串
     * 
     * @param text 待检查文本
     * @return true表示包含模式串，false表示不包含
     */
    public boolean contains(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }

        Node current = root;
        String lowerText = text.toLowerCase();

        for (int i = 0; i < lowerText.length(); i++) {
            char c = lowerText.charAt(i);

            // 跳过空格和特殊字符
            if (!Character.isLetterOrDigit(c) && !isChinese(c)) {
                continue;
            }

            // 沿着失败指针查找匹配
            while (current != root && !current.children.containsKey(c)) {
                current = current.fail;
            }

            if (current.children.containsKey(c)) {
                current = current.children.get(c);
            }

            // 检查是否匹配
            Node temp = current;
            while (temp != root) {
                if (temp.isEnd) {
                    return true;
                }
                temp = temp.fail;
            }
        }

        return false;
    }

    /**
     * 判断字符是否为中文字符
     * 
     * @param c 字符
     * @return true表示是中文字符
     */
    private boolean isChinese(char c) {
        return c >= 0x4E00 && c <= 0x9FA5;
    }
}
