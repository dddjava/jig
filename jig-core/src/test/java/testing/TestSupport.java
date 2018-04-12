package testing;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TestSupport {

    public static URL[] getTestResourceRootURLs() {
        try {
            URL classRootUrl = TestSupport.class.getResource("/DefaultPackageClass.class").toURI().resolve("./").toURL();
            URL resourceRootUrl = TestSupport.class.getResource("/marker.properties").toURI().resolve("./").toURL();
            return new URL[]{classRootUrl, resourceRootUrl};
        } catch (URISyntaxException | MalformedURLException e) {
            throw new AssertionError(e);
        }
    }

    public static Path getModuleRootPath() {
        Path defaultPackageClassPath = defaultPackageClassPath();

        Path path = defaultPackageClassPath.toAbsolutePath();
        while (!path.endsWith("jig-core")) {
            path = path.getParent();
            if (path == null) {
                throw new IllegalStateException("モジュール名変わった？");
            }
        }
        return path;
    }


    private static Path defaultPackageClassPath() {
        try {
            URI uri = TestSupport.class.getResource("/DefaultPackageClass.class").toURI();
            return Paths.get(uri);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
