package jig.classloader;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

public class JarLoader {

    private final String jarFilePath;

    public JarLoader(String jarFilePath) {
        this.jarFilePath = jarFilePath;
    }

    private URLClassLoader getUrlClassLoaders(String jarFilePath) throws MalformedURLException {
        URL[] urls = {new URL("jar:file:" + jarFilePath + "!/")};
        return URLClassLoader.newInstance(urls);
    }

    public Classes findClass(ClassFilter filter) {
        try (JarFile jarFile = new JarFile(jarFilePath);
             URLClassLoader classLoader = getUrlClassLoaders(jarFilePath)) {

            List<Class<?>> classes = jarFile.stream()
                    .filter(jarEntry -> jarEntry.getName().endsWith(".class"))
                    .map(jarEntry -> {
                        try {
                            String className = jarEntry.getName()
                                    .replace(".class", "")
                                    .replaceAll("/", ".");
                            return classLoader.loadClass(className);
                        } catch (ClassNotFoundException e) {
                            throw new IllegalStateException(e);
                        }
                    })
                    .filter(filter::test)
                    .collect(Collectors.toList());
            return new Classes(classes);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
