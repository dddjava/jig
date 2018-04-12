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
            URL classRootUrl = defaultPackageClassURI().resolve("./").toURL();
            URL resourceRootUrl = resourceRootURI().resolve("./").toURL();
            return new URL[]{classRootUrl, resourceRootUrl};
        } catch (MalformedURLException e) {
            throw new AssertionError(e);
        }
    }

    public static Path getModuleRootPath() {
        URI uri = defaultPackageClassURI();
        Path path = Paths.get(uri).toAbsolutePath();

        while (!path.endsWith("jig-core")) {
            path = path.getParent();
            if (path == null) {
                throw new IllegalStateException("モジュール名変わった？");
            }
        }
        return path;
    }


    private static URI defaultPackageClassURI() {
        try {
            return TestSupport.class.getResource("/DefaultPackageClass.class").toURI();
        } catch (URISyntaxException e) {
            throw new AssertionError(e);
        }
    }

    private static URI resourceRootURI() {
        try {
            return TestSupport.class.getResource("/marker.properties").toURI();
        } catch (URISyntaxException e) {
            throw new AssertionError(e);
        }
    }
}
