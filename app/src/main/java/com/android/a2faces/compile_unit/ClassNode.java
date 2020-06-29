package com.android.a2faces.compile_unit;


public class ClassNode extends AbstractNode {
    public static final String CLASS_KEYWORD = "class ";

    public String signature;

    public String className;
    public String modifier;

    public String extendsClassName;
    public String implementsClassName;

    public ClassNode(AbstractNode parent, String signature) {
        super(parent);

        String[] signatureWords = signature.split(" ");
        //this.signature = signature;

        //find class word
        int i = 0;
        while(i < signatureWords.length && !signatureWords[i].equals("class")) {
            i++;
        }

        //read all before class word - public or private
        this.modifier = "";
        for (int j = 0; j < i; j++) {
            this.modifier += signatureWords[j];
        }

        this.className = signatureWords[i+1];

        //after class name there could be extends
        int k = i+1;
        while(k < signatureWords.length && !signatureWords[k].equals("extends")) {
            k++;
        }
        if(k < signatureWords.length) {
            this.extendsClassName = signatureWords[k+1];
        }

        //after class name there could be implements
        k = i+1;
        while(k < signatureWords.length && !signatureWords[k].equals("implements")) {
            k++;
        }
        if(k < signatureWords.length) {
            this.implementsClassName = signatureWords[k+1];
        }
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(this.modifier);
        stringBuilder.append(" ");
        stringBuilder.append(CLASS_KEYWORD);
        stringBuilder.append(" ");
        stringBuilder.append(this.className);
        stringBuilder.append("{ ");
        for (int i = 0; i < childreen.size(); i++) {
            stringBuilder.append(childreen.get(i).toString()).append(" ");
        }
        stringBuilder.append("} ");
        return  stringBuilder.toString();
    }

}

