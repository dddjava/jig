plugins {
   java
   id("org.dddjava.jig-gradle-plugin")
}

jig {
    modelPattern = ".+\\.model\\..+"
    outputOmitPrefix = ".+\\.model\\."
    documentTypes = listOf("PackageRelationDiagram")
}
