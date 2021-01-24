package com.android.app2faces.ast;

public class RootNode extends AbstractNode {
    public RootNode() {
        super(null);
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < childreen.size(); i++) {
            stringBuilder.append(childreen.get(i).toString()).append(" ");
        }
        return stringBuilder.toString();
    }
}
