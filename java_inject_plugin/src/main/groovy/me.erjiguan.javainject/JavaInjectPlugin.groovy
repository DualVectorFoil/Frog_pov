package me.erjiguan.javainject

import com.android.build.gradle.AppExtension

import org.gradle.api.Plugin
import org.gradle.api.Project


public class JavaInjectPlugin implements Plugin<Project> {

    public void apply(Project project) {
        System.out.println("------------------Start----------------------")
        System.out.println("This is my plugin to invoke for frog.")

        def android = project.extensions.getByType(AppExtension)
        def classTransform = new JavaInjectTransform(project)
        android.registerTransform(classTransform)

        System.out.println("------------------End----------------------->")
    }
}
