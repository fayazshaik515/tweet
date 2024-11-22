package com.auth.service;

public class BinarySearchTree<T extends Comparable<T>> {

    private class Node {
        T data;
        Node left, right;

        Node(T data) {
            this.data = data;
        }
    }

    private Node root;

    // Insert data into the BST
    public synchronized void insert(T data) {
        if (data == null) {
            throw new IllegalArgumentException("Cannot insert null into the BST");
        }
        root = insertRec(root, data);
        System.out.println("Inserted: " + data);
    }

    private Node insertRec(Node node, T data) {
        if (node == null) {
            return new Node(data);
        }
        if (data.compareTo(node.data) < 0) {
            node.left = insertRec(node.left, data);
        } else if (data.compareTo(node.data) > 0) {
            node.right = insertRec(node.right, data);
        }
        return node;
    }

    // Search for data in the BST
    public synchronized T search(T data) {
        if (data == null) {
            throw new IllegalArgumentException("Cannot search for null in the BST");
        }
        T result = searchRec(root, data);
        if (result == null) {
            System.out.println("Search failed: " + data + " not found");
        } else {
            System.out.println("Search successful: " + result);
        }
        return result;
    }

    private T searchRec(Node node, T data) {
        if (node == null) {
            return null;
        }
        if (data.compareTo(node.data) == 0) {
            return node.data;
        }
        return data.compareTo(node.data) < 0
            ? searchRec(node.left, data)
            : searchRec(node.right, data);
    }

    // Clear the BST
    public synchronized void clear() {
        root = null;
        System.out.println("BST cleared");
    }
}
