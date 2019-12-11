package org.dddjava.sig.cli

import org.dddjava.jig.domain.model.interpret.alias.SourceCodeAliasReader
import org.dddjava.jig.infrastructure.configuration.{Configuration, JigProperties, OutputOmitPrefix}
import org.dddjava.jig.infrastructure.javaparser.JavaparserAliasReader
import org.dddjava.jig.infrastructure.resourcebundle.Utf8ResourceBundle

object Main extends App {

  val jigMessages = Utf8ResourceBundle.messageBundle()
  val configuration = new Configuration(
    new JigProperties(
      ".+\\.domain\\.(model|type)\\..+",
      new OutputOmitPrefix(".+\\.(service|domain\\.(model|type))\\.")
  ),
    new SourceCodeAliasReader(new JavaparserAliasReader())
  )

  val implementationService = configuration.implementationService()
  val jigDocumentHandlers = configuration.documentHandlers()

  val sourcePaths = cliConfig.rawSourceLocations
  val implementations = implementationService.implementations(sourcePaths)

  val status = implementations.status
  if (status.hasError) {
    import scala.collection.JavaConversions._
    for (analyzeStatus <- status.listErrors) {
      LOGGER.warn(jigMessages.getString("failure.details"), jigMessages.getString(analyzeStatus.messageKey))
    }
    return
  }
  if (status.hasWarning) {
    LOGGER.warn(jigMessages.getString("implementation.warning"))
    import scala.collection.JavaConversions._
    for (analyzeStatus <- status.listWarning) {
      LOGGER.warn(jigMessages.getString("implementation.warning.details"), jigMessages.getString(analyzeStatus.messageKey))
    }
  }

  val handleResultList = new Nothing
  val outputDirectory = cliConfig.outputDirectory

  import scala.collection.JavaConversions._

  for (jigDocument <- jigDocuments) {
    val result = jigDocumentHandlers.handle(jigDocument, new Nothing(implementations), outputDirectory)
    handleResultList.add(result)
  }

  val resultLog = handleResultList.stream.filter(HandleResult.success).map((handleResult) => handleResult.jigDocument + " : " + handleResult.outputFilePaths).collect(Collectors.joining("\n"))


}
