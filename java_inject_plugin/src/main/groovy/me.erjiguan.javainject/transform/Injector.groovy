package me.erjiguan.javainject.transform

import javassist.ClassPool
import javassist.CtClass
import javassist.CtField
import javassist.CtMethod
import org.gradle.api.Project

import java.lang.reflect.Modifier

class Injector {

    private final static String PACKAGE_NAME = "com/erjiguan/frog"

    private final static ClassPool mPool = new ClassPool(true)

    protected static void inject(String path, Project project) throws Exception {
        mPool.appendClassPath(path)
        mPool.appendClassPath(project.android.bootClasspath[0].toString())
        mPool.insertClassPath("/Users/diode/Library/Android/sdk/platforms/android-29/android.jar")

        println ">>>>>>>>>>>>>>>>>>>start inject>>>>>>>>>>>>>>>>>>>>"

        File dir = new File(path)
        if (dir.isDirectory()) {
            dir.eachFileRecurse { file ->
                def filePath = file.absolutePath
                if (filePath.endsWith(".class")
                    && !filePath.contains('R$')
                    && !filePath.contains('R.class')
                    && !filePath.contains("BuildConfig.class")
                ) {
                    println "class file path: " + filePath

                    int index = filePath.indexOf(PACKAGE_NAME)
                    println "index in class file path: " + index
                    if (index == -1) {
                        return
                    }

                    int end = filePath.length() - 6
                    String className = filePath.substring(index, end).replace('\\', '.').replace('/', '.')
                    println "class name: " + className

                    CtClass ctClass = mPool.getCtClass(className)
                    println "has get ctClass, name: " + ctClass.getName()
                    if (ctClass.isFrozen()) {
                        ctClass.defrost()
                    }

                    CtMethod[] ctMethods = ctClass.getDeclaredMethods()
                    println "ctMethods count: " + ctMethods.size()
                    for (CtMethod ctMethod : ctMethods) {
                        println ">>>>>>>>>>>>>>>>begin inject method>>>>>>>>>>>>>>"

                        if (ctClass.getName().equals("com.erjiguan.frog.invokehelper.InvokeHelper")) {
                            println ctClass.getName()
                            println ">>>>>>>>>>self not inject continue>>>>>>>>"
                            continue
                        }

                        String methodName = ctMethod.getName()
                        String returnType = ctMethod.getReturnType().getName()
                        boolean isStatic = Modifier.isStatic(ctMethod.getModifiers())
                        StringBuffer invokeBody = new StringBuffer()
                        invokeBody.append("\nif (com.erjiguan.frog.invokehelper.InvokeHelper.ENABLE_INVOKE) {\n")
                        if (ctMethod.getParameterTypes().size() > 0) {
                            invokeBody.append('java.lang.Object[] hookArgs = $args;\n')
                        }

                        if (!'void'.equals(returnType)) {
                            invokeBody.append('return (' + returnType + ') com.erjiguan.frog.invokehelper.InvokeHelper.invoke(')
                        } else {
                            invokeBody.append('com.erjiguan.frog.invokehelper.InvokeHelper.invoke(')
                        }

                        if (isStatic) {
                            invokeBody.append('null, ')
                        } else {
                            invokeBody.append('this, ')
                        }

                        invokeBody.append('\"' + className + '\"' + ', ')

                        invokeBody.append('\"' + methodName + '\"' + ', ')

                        if (ctMethod.getParameterTypes().size() > 0) {
                            invokeBody.append('hookArgs, ')
                        } else {
                            invokeBody.append('null, ')
                        }

                        invokeBody.append('android.os.Process.myPid(), ')

                        invokeBody.append('android.os.Process.myTid(), ')

                        invokeBody.append('android.os.Process.myUid(), ')

                        invokeBody.append('System.currentTimeMillis() / 1000);\n')

                        if ('void'.equals(returnType)) {
                            invokeBody.append('return;\n')
                        }

                        invokeBody.append('}\n')

                        println invokeBody.toString()

                        ctMethod.insertBefore(invokeBody.toString())
//                        ctMethod.insertBefore('\nif (true) {\nSystem.out.println(123);\nSystem.out.println(1);\nSystem.out.println(2);\nSystem.out.println(10);}')

                        println ">>>>>>>>>>>>>>>>stop inject method>>>>>>>>>>>>>>>>"
                    }
                    ctClass.writeFile(path)
                }
            }
        }

        println ">>>>>>>>>>>>>>>>>>>end inject>>>>>>>>>>>>>>>>>>>>>>"
    }
}
