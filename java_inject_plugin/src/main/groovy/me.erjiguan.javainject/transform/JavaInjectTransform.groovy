package me.erjiguan.javainject

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import me.erjiguan.javainject.transform.Injector
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.gradle.api.Project

/**
 * Created by 刘镓旗 on 2017/8/30.
 */

public class JavaInjectTransform extends Transform {

    private Project mProject;

    public JavaInjectTransform(Project p) {
        this.mProject = p;
    }

    //transform的名称
    //transformClassesWithMyClassTransformForDebug 运行时的名字
    //transformClassesWith + getName() + For + Debug或Release
    @Override
    public String getName() {
        return "JavaInjectTransform";
    }

    //需要处理的数据类型，有两种枚举类型
    //CLASSES和RESOURCES，CLASSES代表处理的java的class文件，RESOURCES代表要处理java的资源
    @Override
    public Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS;
    }

//    指Transform要操作内容的范围，官方文档Scope有7种类型：
//
//    EXTERNAL_LIBRARIES        只有外部库
//    PROJECT                       只有项目内容
//    PROJECT_LOCAL_DEPS            只有项目的本地依赖(本地jar)
//    PROVIDED_ONLY                 只提供本地或远程依赖项
//    SUB_PROJECTS              只有子项目。
//    SUB_PROJECTS_LOCAL_DEPS   只有子项目的本地依赖项(本地jar)。
//    TESTED_CODE                   由当前变量(包括依赖项)测试的代码
    @Override
    public Set<QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT;
    }

    @Override
    public boolean isIncremental() {
        return false
    }

    @Override
    public void transform(Context context,
                          Collection inputs,
                          Collection referencedInputs,
                          TransformOutputProvider outputProvider,
                          boolean isIncremental) throws IOException, TransformException, InterruptedException {
        System.out.println("----------------Enter transform--------------")

        if (!isIncremental) {
            outputProvider.deleteAll()
        }

        // TODO incremental
        inputs.each { TransformInput input ->
            input.directoryInputs.each { DirectoryInput directoryInput ->
                try {
                    Injector.inject(directoryInput.file.absolutePath, mProject)
                } catch (Exception e) {
                    println "class inject failed, err: " + e.toString()
                }

                def dest = outputProvider.getContentLocation(directoryInput.name,
                        directoryInput.contentTypes, directoryInput.scopes, Format.DIRECTORY)

                FileUtils.copyDirectory(directoryInput.file, dest)
            }

            // TODO Do not process jar for now, maybe later
            input.jarInputs.each { JarInput jarInput ->
                def jarName = jarInput.name
                println("jar = " + jarInput.file.getAbsolutePath())
                def md5Name = DigestUtils.md5Hex(jarInput.file.getAbsolutePath())
                if (jarName.endsWith(".jar")) {
                    jarName = jarName.substring(0, jarName.length() - 4)
                }
                def dest = outputProvider.getContentLocation(jarName + md5Name, jarInput.contentTypes, jarInput.scopes, Format.JAR)
                FileUtils.copyFile(jarInput.file, dest)
            }
        }
        System.out.println("--------------End transform----------------")
    }
}
