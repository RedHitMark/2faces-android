package com.android.a2faces.compile_unit;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

public class JavaParser {
    private String sourceCode;

    private SyntaxTree parserdFile;

    public JavaParser(String pSourceCode) throws NotBalancedParenthesisException, InvalidSourceCodeException {
        if(!JavaParser.areParanthesisBalanced(pSourceCode)) {
            throw new NotBalancedParenthesisException();
        }
        this.sourceCode = pSourceCode;

        this.parserdFile = new SyntaxTree();

        this.buildAST();
    }

    public void buildAST() throws InvalidSourceCodeException{
        this.parserdFile.root = parser(this.sourceCode, 0, this.sourceCode.length(), this.parserdFile.getRoot());
    }

    /**
     * Recursive function to parse java code in a {@link AbstractNode} Tree
     *
     * @param code: sting with javacode
     * @param start: index of first word of block
     * @param end: index of end of block
     * @param root: block to parse
     *
     * @return the block parsed
     */
    private AbstractNode parser(String code, int start, int end, AbstractNode root) throws InvalidSourceCodeException {
        Stack<Character> stack = new Stack<>();
        int i = start;
        while ( i < end ) {
            char c = code.charAt(i);

            if (c == ';') {
                // this is a statement or an import
                StringBuilder a = new StringBuilder();
                while (!stack.empty()) {
                    a.append(stack.pop());
                }
                String statement = a.reverse().toString().trim();

                if( statement.startsWith("import ")) {
                    AbstractNode importNode = new ImportNode(root, statement);
                    root.addChild(importNode);
                } else {
                    AbstractNode statementNode = new StatementNode(root, statement);
                    root.addChild(statementNode);
                }
            } else if (c == '{') {
                //this is the beginning of an inner block
                //get the signature
                StringBuilder signatureBuilder = new StringBuilder();
                while (!stack.empty()) {
                    signatureBuilder.append(stack.pop());
                }
                String signature = signatureBuilder.reverse().toString().trim();

                //search for class word in signature
                String[] signatureWords = signature.split(" ");
                int j = 0;
                while (j < signatureWords.length && !signatureWords[j].equals("class")) {
                    j++;
                }
                int endOfBlock = findEndOfBlock(code, i);

                if (j < signatureWords.length) {
                    // parse class block and add as child
                    AbstractNode classNode = new ClassNode(root, signature);
                    root.addChild(parser(code, i+1, endOfBlock, classNode));
                } else if (root instanceof ClassNode) {
                    // valid method block, check for constructor or  method
                    if ( signature.contains( " " + ((ClassNode) root).className + "(") || signature.contains( " " + ((ClassNode) root).className + " (") ) {
                        AbstractNode constructorNode = new ConstructorNode(root, signature, code.substring(i, endOfBlock));
                        root.childreen.add(constructorNode);
                    } else {
                        AbstractNode methodNode = new MethodNode(root, signature, code.substring(i, endOfBlock));
                        root.childreen.add(methodNode);
                    }
                } else {
                    throw new InvalidSourceCodeException();
                }

                i = endOfBlock;
            } else {
                stack.push(c);
            }
            i++;
        }

        return root;
    }

    /**
     * Assumption: import statement can be only in first level of tree
     * Assumption: every import statement starts with "import" keyword
     * Assumption: no static import will be in file
     *
     * @return a list of import statement
     */
    public List<String> getImportPackagesPathList() {
        List<String> importStatement = new ArrayList<>();

        for (int i = 0; i < this.parserdFile.root.childreen.size(); i++) {
            AbstractNode abstractNode = this.parserdFile.root.childreen.get(i);
            if(abstractNode instanceof ImportNode) {
                importStatement.add(((ImportNode) abstractNode).packagePath);
            }
        }

        return importStatement;
    }

    public List<ClassNode> getParsedClassList(AbstractNode root) {
        List<ClassNode> parsedClasses = new ArrayList<>();
        for (int i = 0; i < root.childreen.size(); i++) {
            AbstractNode abstractNode = root.childreen.get(i);
            if(abstractNode instanceof ClassNode) {
                parsedClasses.add((ClassNode) abstractNode);
            }
        }

        return parsedClasses;
    }

    public List<ConstructorNode> getParsedConstructorList(ClassNode parsedClass) {
        List<ConstructorNode> parsedConstructors = new ArrayList<>();
        for (int i = 0; i < parsedClass.childreen.size(); i++) {
            AbstractNode abstractNode = parsedClass.childreen.get(i);
            if(abstractNode instanceof ConstructorNode) {
                parsedConstructors.add((ConstructorNode) abstractNode);
            }
        }

        return parsedConstructors;
    }

    public List<MethodNode> getParsedMethodList(ClassNode parsedClass) {
        List<MethodNode> parsedMethods = new ArrayList<>();
        for (int i = 0; i < parsedClass.childreen.size(); i++) {
            AbstractNode abstractNode = parsedClass.childreen.get(i);
            if(abstractNode instanceof MethodNode) {
                parsedMethods.add((MethodNode) abstractNode);
            }
        }

        return parsedMethods;
    }

    public SyntaxTree getParserdFile() {
        return parserdFile;
    }

    private static int findEndOfBlock(String code, int start) {
        Stack<Character> stack = new Stack<>();

        while(code.charAt(start) != '{') {
            start++;
        }

        do {
            char c = code.charAt(start);

            if (c == '{' || c == '(' || c == '[') {
                stack.push(c);
            }

            if (c == '}' || c == ')' || c == ']') {
                stack.pop();
            }
            start++;
        } while(!stack.empty());

        return start;
    }

    private static boolean areParanthesisBalanced(String sourceCodeToCheck) {
        Stack<Character> stack = new Stack<>();

        for (int i = 0; i < sourceCodeToCheck.length(); i++) {
            char c = sourceCodeToCheck.charAt(i);

            if (c == '{' || c == '(' || c == '[') {
                stack.push(c);
            }

            if (c == '}' || c == ')' || c == ']') {
                if (stack.empty()) {
                    return false;
                }

                char prv = stack.pop();

                if ( !isMatchingPair(prv, c) ) {
                    return false;
                }
            }
        }
        return stack.empty();
    }

    private static boolean isMatchingPair(char c1, char c2) {
        return (c1 == '(' && c2 == ')') || (c1 == '{' && c2 == '}') || (c1 == '[' && c2 == ']');
    }
}
