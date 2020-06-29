package com.android.a2faces.compile_unit;

public class StatementNode extends AbstractNode {
    String code;

    public StatementNode(AbstractNode parent, String code) {
        super(parent);
        this.code = code;
    }

    @Override
    public String toString() {
        return this.code + " ";
    }
}
