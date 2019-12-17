package org.dddjava.sig.cli

import java.io.IOException
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.{ FileVisitResult, Path, SimpleFileVisitor }
import java.util.Objects

import org.slf4j.LoggerFactory
import scala.collection.mutable

class DirectoryCollector(filter: Path => Boolean) extends SimpleFileVisitor[Path] {

  private val LOGGER                      = LoggerFactory.getLogger(classOf[DirectoryCollector])
  private val paths: mutable.Buffer[Path] = mutable.Buffer.empty

  override def preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult =
    if (filter(dir)) {
      paths.addOne(dir)
      LOGGER.debug("classes: {}", dir)
      FileVisitResult.SKIP_SUBTREE
    } else
      FileVisitResult.CONTINUE

  override def postVisitDirectory(dir: Path, exc: IOException): FileVisitResult = {
    Objects.requireNonNull(dir)
    if (exc != null) LOGGER.warn("skipped '{}'. (type={}, message={})", dir, exc.getClass.getName, exc.getMessage)
    FileVisitResult.CONTINUE
  }

  override def visitFileFailed(file: Path, exc: IOException): FileVisitResult = {
    Objects.requireNonNull(file)
    LOGGER.warn("skipped '{}'. (type={}, message={})", file, exc.getClass.getName, exc.getMessage)
    FileVisitResult.CONTINUE
  }

  def listPath: Seq[Path] = paths.toSeq
}
