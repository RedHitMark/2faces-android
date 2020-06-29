package com.android.a2faces.compile_unit;

public class MethodNode extends AbstractNode {
    String signature;
    String body;

    public MethodNode(AbstractNode parent, String signature, String body) {
        super(parent);
        this.signature = signature;
        this.body = body;
    }

    @Override
    public String toString() {
        return this.signature + " " + this.body;
    }
}
