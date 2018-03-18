package jig.infrastructure;

import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class JigPaths {

    public boolean isJavaFile(Path path) {
        return path.toString().endsWith(".java");
    }

    public boolean isClassFile(Path path) {
        return path.toString().endsWith(".class");
    }

    public boolean isPackageInfoFile(Path path) {
        return path.toString().endsWith("package-info.java");
    }

    public boolean isGradleClassPathRootDirectory(Path path) {
        return path.endsWith(Paths.get("build", "classes", "java", "main"))
                || path.endsWith(Paths.get("build", "resources", "main"));
    }

    public boolean isMapperClassFile(Path path) {
        return path.toString().endsWith("Mapper.class");
    }

    public String toClassName(Path path) {
        String pathStr = path.toString();
        return pathStr.substring(0, pathStr.length() - 6).replace('/', '.');
    }
}
