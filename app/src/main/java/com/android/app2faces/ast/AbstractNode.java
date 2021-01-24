package com.android.app2faces.ast;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractNode {
    public AbstractNode parent;
    public List<AbstractNode> childreen;

    public AbstractNode(AbstractNode parent) {
        this.parent = parent;
        this.childreen = new ArrayList<>();
    }

    public void addChild(AbstractNode child) {
        this.childreen.add(child);
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
