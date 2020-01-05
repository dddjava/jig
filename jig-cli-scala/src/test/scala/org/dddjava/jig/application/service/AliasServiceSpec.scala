package org.dddjava.jig.application.service

import java.net.URI
import java.nio.file.{Path, Paths}
import java.util.Collections

import org.dddjava.jig.domain.model.declaration.`type`.TypeIdentifier
import org.dddjava.jig.domain.model.declaration.method.{Arguments, MethodIdentifier, MethodSignature}
import org.dddjava.jig.domain.model.jigloaded.alias.SourceCodeAliasReader
import org.dddjava.jig.domain.model.jigsource.source.binary.BinarySourcePaths
import org.dddjava.jig.domain.model.jigsource.source.code.CodeSourcePaths
import org.dddjava.jig.domain.model.jigsource.source.{SourcePaths, Sources}
import org.dddjava.jig.infrastructure.ScalametaAliasReader
import org.dddjava.jig.infrastructure.filesystem.LocalFileSourceReader
import org.dddjava.jig.infrastructure.javaparser.JavaparserAliasReader
import org.dddjava.jig.infrastructure.onmemoryrepository.OnMemoryAliasRepository
import org.scalatest._
import stub.domain.model.ScalaMethodScaladocStub.ObjectInObject.ObjectInObjectInObject
import stub.domain.model.ScalaMethodScaladocStub.SealedTrait
import stub.domain.model.pkg1.PackageObjectTrait
import stub.domain.model.{ScalaMethodScaladocStub, ScalaStub}

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

    "Scalaクラス別名取得" in {
      val source = getTestRawSource
      sut.loadAliases(source.aliasSource())

      val typeAlias1 = sut.typeAliasOf(new TypeIdentifier(classOf[ScalaStub]))
      assert(typeAlias1.asText() === "ScalaのクラスのDoc")

      val typeAlias2 = sut.typeAliasOf(new TypeIdentifier(classOf[ScalaMethodScaladocStub]))
      assert(typeAlias2.asText() === "ScalaのTraitのDoc")

      val typeAlias3 = sut.typeAliasOf(new TypeIdentifier("stub.domain.model.ScalaMethodScaladocStub$"))
      assert(typeAlias3.asText() === "ScalaのObjectのDoc")

      val typeAlias4 = sut.typeAliasOf(new TypeIdentifier(classOf[SealedTrait]))
      assert(typeAlias4.asText() === "Object内のTrait")

      val typeAlias5 = sut.typeAliasOf(new TypeIdentifier(classOf[ObjectInObjectInObject]))
      assert(typeAlias5.asText() === "Objectの中のObjectの中のObject")

      val typeAlias6 = sut.typeAliasOf(new TypeIdentifier(classOf[PackageObjectTrait]))
      assert(typeAlias6.asText() === "パッケージObjectのTrait")
    }

//    "Scalaメソッドの和名取得" in {
//      val source = getTestRawSource
//      sut.loadAliases(source.aliasSource())
//
//      val typeIdentifier = new TypeIdentifier(classOf[ScalaMethodScaladocStub])
//      val identifier1    = new MethodIdentifier(typeIdentifier, new MethodSignature("simpleMethod", Arguments.empty()))
//      val methodAlias1   = sut.methodAliasOf(identifier1)
//      assert(methodAlias1.asText() === "メソッドのドキュメント")
//    }

  }

}
