package org.dddjava.sig.cli

import java.nio.file.Path

import org.dddjava.jig.infrastructure.resourcebundle.Utf8ResourceBundle
import org.dddjava.jig.presentation.view.handler.HandlerMethodArgumentResolver
import org.slf4j.LoggerFactory

import scala.jdk.CollectionConverters._

object Main extends App {
  private val LOGGER = LoggerFactory.getLogger("Main")

  private val cliConfig = ScalaCliConfig()

  private val jigMessages   = Utf8ResourceBundle.messageBundle()
  private val jigDocuments  = cliConfig.jigDocuments()
  private val configuration = cliConfig.configuration()

  LOGGER.info(
    "-- configuration -------------------------------------------\n{}\n------------------------------------------------------------",
    cliConfig.propertiesText()
  )

  val startTime = System.currentTimeMillis

  private val implementationService = configuration.implementationService
  private val jigDocumentHandlers   = configuration.documentHandlers

  private val sourcePaths     = cliConfig.rawSourceLocations()
  private val implementations = implementationService.implementations(sourcePaths)

  private val status = implementations.status
  if (status.hasError) {
    LOGGER.warn(jigMessages.getString("failure"))
    status.listErrors().asScala.foreach { analyzeStatus =>
      LOGGER.warn(jigMessages.getString("failure.details"), jigMessages.getString(analyzeStatus.messageKey))
    }
    throw new RuntimeException("failure")
  }

  if (status.hasWarning) {
    LOGGER.warn(jigMessages.getString("implementation.warning"))
    status.listWarning().asScala.foreach { analyzeStatus =>
      LOGGER
        .warn(jigMessages.getString("implementation.warning.details"), jigMessages.getString(analyzeStatus.messageKey))
    }
  }

  private val outputDirectory: Path = cliConfig.outputDirectory()

  private val handleResultList =
    jigDocuments.map { jigDocument =>
      jigDocumentHandlers.handle(jigDocument, new HandlerMethodArgumentResolver(implementations), outputDirectory)
    }

  val resultLog = handleResultList
    .filter(_.success)
    .map { handleResult =>
      handleResult.jigDocument + " : " + handleResult.outputFilePaths
    }.mkString("\n")

  LOGGER.info(
    "-- output documents -------------------------------------------\n{}\n------------------------------------------------------------",
    resultLog
  )
  LOGGER.info(jigMessages.getString("success"), System.currentTimeMillis - startTime)

}
