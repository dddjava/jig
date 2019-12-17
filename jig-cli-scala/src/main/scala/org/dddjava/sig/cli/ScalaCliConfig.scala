package org.dddjava.sig.cli

import java.io.{IOException, UncheckedIOException}
import java.nio.file.{Files, Path, Paths}
import java.util.StringJoiner

import com.typesafe.config.ConfigFactory
import org.dddjava.jig.domain.model.jigdocument.JigDocument
import org.dddjava.jig.domain.model.jigloaded.alias.SourceCodeAliasReader
import org.dddjava.jig.domain.model.jigsource.source.SourcePaths
import org.dddjava.jig.domain.model.jigsource.source.binary.BinarySourcePaths
import org.dddjava.jig.domain.model.jigsource.source.code.CodeSourcePaths
import org.dddjava.jig.infrastructure.configuration.{Configuration, JigProperties, OutputOmitPrefix}
import org.dddjava.jig.infrastructure.javaparser.JavaparserAliasReader

import scala.jdk.CollectionConverters._

case class ScalaCliConfig() {

  private val config = ConfigFactory.load()
  private val documentTypeText    = config.getString("documentType")
  private val outputDirectoryText = config.getString("outputDirectory")

  private val outputOmitPrefix = config.getString("output.omit.prefix")
  private val modelPattern     = config.getString("jig.model.pattern")

  private val projectPath        = config.getString("project.path")
  private val directoryClasses   = config.getString("directory.classes")
  private val directoryResources = config.getString("directory.resources")
  private val directorySources   = config.getString("directory.sources")

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
