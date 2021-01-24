package com.android.app2faces.ast;

public class ConstructorNode extends AbstractNode {
    public String signature;
    public String body;

    public ConstructorNode(AbstractNode parent, String signature, String body) {
        super(parent);

        this.signature = signature;
        this.body = body;
    }

    @Override
    public String toString() {
        return this.signature + this.body;
    }
}
