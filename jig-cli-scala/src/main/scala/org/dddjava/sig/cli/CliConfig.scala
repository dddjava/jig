package org.dddjava.sig.cli

import java.io.{ IOException, UncheckedIOException }
import java.nio.file.{ Files, Path, Paths }
import java.util.StringJoiner

import org.dddjava.jig.domain.model.diagram.JigDocument
import org.dddjava.jig.domain.model.implementation.source.SourcePaths
import org.dddjava.jig.domain.model.implementation.source.binary.BinarySourcePaths
import org.dddjava.jig.domain.model.implementation.source.code.CodeSourcePaths
import org.dddjava.jig.domain.model.interpret.alias.SourceCodeAliasReader
import org.dddjava.jig.infrastructure.configuration.{ Configuration, JigProperties, OutputOmitPrefix }
import org.dddjava.jig.infrastructure.javaparser.JavaparserAliasReader

import scala.jdk.CollectionConverters._

case class CliConfig() {

  private val documentTypeText    = ""
  private val outputDirectoryText = "./build/jig"

  private val outputOmitPrefix = ".+\\.(service|domain\\.(model|type))\\."
  private val modelPattern     = ".+\\.domain\\.(model|type)\\..+"

  private val projectPath        = "/Users/yoshiyoshifujii/workspace/git/dddjava/Jig/jig-core"
  private val directoryClasses   = "build/classes/java/main"
  private val directoryResources = "build/resources/main"
  private val directorySources   = "src/main/java"

  def propertiesText(): String =
    new StringJoiner("\n")
      .add("documentType=" + documentTypeText)
      .add("outputDirectory=" + outputDirectory)
      .add("output.omit.prefix=" + outputOmitPrefix)
      .add("jig.model.pattern=" + modelPattern)
      .add("project.path=" + projectPath)
      .add("directory.classes=" + directoryClasses)
      .add("directory.resources=" + directoryResources)
      .add("directory.sources=" + directorySources)
      .toString

  def outputDirectory(): Path = Paths.get(outputDirectoryText)

  def rawSourceLocations(): SourcePaths =
    try {
      val projectRoot = Paths.get(projectPath)
      val binaryCollector = new DirectoryCollector(
        path => path.endsWith(directoryClasses) || path.endsWith(directoryResources)
      )
      Files.walkFileTree(projectRoot, binaryCollector)
      val binarySourcePaths = binaryCollector.listPath

      val sourceCollector = new DirectoryCollector(_.endsWith(directorySources))
      Files.walkFileTree(projectRoot, sourceCollector)
      val textSourcesPaths = sourceCollector.listPath
      new SourcePaths(
        new BinarySourcePaths(binarySourcePaths.asJava),
        new CodeSourcePaths(textSourcesPaths.asJava)
      )
    } catch {
      case e: IOException =>
        throw new UncheckedIOException(e)
    }

  def configuration(): Configuration =
    new Configuration(
      new JigProperties(
        modelPattern,
        new OutputOmitPrefix(outputOmitPrefix)
      ),
      new SourceCodeAliasReader(new JavaparserAliasReader())
    )

  def jigDocuments(): Seq[JigDocument] =
    if (documentTypeText.isEmpty)
      JigDocument.canonical().asScala.toSeq
    else
      JigDocument.resolve(documentTypeText).asScala.toSeq

}
