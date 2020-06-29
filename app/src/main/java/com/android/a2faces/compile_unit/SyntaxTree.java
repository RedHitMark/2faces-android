package com.android.a2faces.compile_unit;

public class SyntaxTree {
    AbstractNode root;

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
