package com.android.app2faces.ast;

public class StatementNode extends AbstractNode {
    public String code;

    public StatementNode(AbstractNode parent, String code) {
        super(parent);
        this.code = code;
    }

    @Override
    public String toString() {
        return this.code + " ";
    }
}
