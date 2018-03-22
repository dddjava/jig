package testing;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

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
}
