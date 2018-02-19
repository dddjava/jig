package jig.application.service;

import jig.analizer.classloader.ClassFilter;
import jig.analizer.classloader.Classes;
import jig.analizer.classloader.JarLoader;

import java.lang.annotation.Annotation;
import java.util.Arrays;

public class JarLoadService {

    public Classes findServiceClasses(String jarFilePath) {
        JarLoader jarLoader = new JarLoader(jarFilePath);
        ClassFilter filter = clz ->
                Arrays.stream(clz.getAnnotations())
                        .map(Annotation::annotationType)
                        .anyMatch(type -> type.getName().equals(org.springframework.stereotype.Service.class.getName()));
        return jarLoader.findClass(filter);
    }
}
