package org.dddjava.jig.application.service

import java.net.URI
import java.nio.file.{ Path, Paths }
import java.util.Collections

import org.dddjava.jig.domain.model.declaration.`type`.TypeIdentifier
import org.dddjava.jig.domain.model.jigloaded.alias.SourceCodeAliasReader
import org.dddjava.jig.domain.model.jigsource.source.{ SourcePaths, Sources }
import org.dddjava.jig.domain.model.jigsource.source.binary.BinarySourcePaths
import org.dddjava.jig.domain.model.jigsource.source.code.CodeSourcePaths
import org.dddjava.jig.infrastructure.ScalametaAliasReader
import org.dddjava.jig.infrastructure.filesystem.LocalFileSourceReader
import org.dddjava.jig.infrastructure.javaparser.JavaparserAliasReader
import org.dddjava.jig.infrastructure.onmemoryrepository.OnMemoryAliasRepository
import org.scalatest._

class AliasServiceSpec extends FreeSpec {

  "AliasService" - {
    lazy val sut: AliasService =
      new AliasService(new SourceCodeAliasReader(new JavaparserAliasReader(), new ScalametaAliasReader()),
                       new OnMemoryAliasRepository())
    lazy val defaultPackageClassURI: URI = this.getClass.getResource("/DefaultPackageClass.class").toURI.resolve("./")
    lazy val getModuleRootPath: Path = {
      var path = Paths.get(defaultPackageClassURI).toAbsolutePath
      while (!path.endsWith("jig-cli-scala")) {
        path = path.getParent
        if (path == null) throw new RuntimeException("モジュール名変わった？")
      }
      path
    }
    lazy val getRawSourceLocations: SourcePaths =
      new SourcePaths(
        new BinarySourcePaths(Collections.singletonList(Paths.get(defaultPackageClassURI))),
        new CodeSourcePaths(
          Collections.singletonList(getModuleRootPath.resolve("src").resolve("test").resolve("scala"))
        )
      )
    lazy val getTestRawSource: Sources = {
      new LocalFileSourceReader().readSources(getRawSourceLocations)
    }

    "クラス別名取得" in {
      val source      = getTestRawSource
      sut.loadAliases(source.aliasSource())
      assert(sut.typeAliasOf(new TypeIdentifier(classOf[stub.domain.model.ScalaStub])).asText() === "ScalaのクラスのDoc")
    }

  }

}
