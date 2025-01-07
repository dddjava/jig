package testing.xlsx;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class XlsxAssertions {

    public static void assertTextValues(Path path, List<List<String>> expected) {
        try (var zipInputStream = new ZipInputStream(Files.newInputStream(path))) {
            ZipEntry zipEntry;
            var stringList = new ArrayList<String>();
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {

                if (zipEntry.getName().equals("xl/sharedStrings.xml")) {
                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder builder = factory.newDocumentBuilder();
                    Document doc = builder.parse(new ByteArrayInputStream(zipInputStream.readAllBytes()));

                    var nodeList = doc.getElementsByTagName("t");
                    for (int i = 0; i < nodeList.getLength(); i++) {
                        stringList.add(nodeList.item(i).getTextContent());
                    }
                }

                if (zipEntry.getName().equals("xl/worksheets/sheet1.xml")) {

                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder builder = factory.newDocumentBuilder();
                    Document doc = builder.parse(new ByteArrayInputStream(zipInputStream.readAllBytes()));

                    var actual = new ArrayList<List<String>>();
                    var rowNodes = doc.getElementsByTagName("row");
                    for (int i = 0; i < rowNodes.getLength(); i++) {
                        var cellNodes = (NodeList) rowNodes.item(i);
                        var rowStrings = new ArrayList<String>();
                        for (int j = 0; j < cellNodes.getLength(); j++) {
                            var cellNode = cellNodes.item(j);
                            var cellAddress = cellNode.getAttributes().getNamedItem("r").getTextContent();
                            var index = Integer.parseInt(cellNode.getFirstChild().getTextContent());

                            rowStrings.add(stringList.get(index));
                        }
                        actual.add(rowStrings);
                    }
                    assertEquals(expected, actual);
                }
            }
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }
}
