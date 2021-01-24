package com.android.app2faces.ast;

public class SyntaxTree {
    public AbstractNode root;

    public SyntaxTree() {
        this.root = new RootNode();
    }

    public AbstractNode getRoot() {
        return root;
    }


    @Override
    public String toString() {
        return root.toString();
    }
}
