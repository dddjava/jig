package org.dddjava.jig.infrastructure

import java.nio.charset.StandardCharsets
import java.util.Collections

import org.dddjava.jig.domain.model.declaration.`type`.TypeIdentifier
import org.dddjava.jig.domain.model.jigloaded.alias.{Alias, JavadocAliasSource, ScalaSourceAliasReader, TypeAlias, TypeAliases}
import org.dddjava.jig.domain.model.jigsource.source.code.scalacode.ScalaSources

import scala.jdk.CollectionConverters._
import scala.meta._
import scala.meta.contrib._

class ScalametaAliasReader() extends ScalaSourceAliasReader {

  case class Documentable(maybePkg: Option[Pkg], tree: Tree, doxText: Option[List[DocToken]]) {
    private lazy val packageName: String = maybePkg.map(p => s"${p.ref.syntax}.").getOrElse("")
    private lazy val fullName: String = s"$packageName${tree.name}"
    lazy val typeIdentifier: TypeIdentifier = new TypeIdentifier(fullName)
    lazy val maybeAlias: Option[Alias] = doxText match {
      case Some(DocToken(DocToken.Description, Some(name), _) :: _) => Some(new JavadocAliasSource(name).toAlias)
      case Some(DocToken(DocToken.Description, _, Some(body)) :: _) => Some(new JavadocAliasSource(body).toAlias)
      case _ => None
    }
  }

  implicit class RichTree(tree: Tree) {

    def isClassOrObjectOrTrait: Boolean = tree match {
      case _: Defn.Class  => true
      case _: Defn.Object => true
      case _: Defn.Trait  => true
      case _              => false
    }

    def name: String = tree match {
      case d: Defn.Class  => d.name.value
      case d: Defn.Object => s"${d.name.value}$$"
      case d: Defn.Trait  => d.name.value
      case _ => ""
    }
  }

  private def extractFromTree(maybePkg: Option[Pkg], tree: Tree): List[Documentable] = {
    val comments = AssociatedComments(tree)

    def parsedScaladocComment(tree: Tree): Option[Documentable] =
      (tree.isClassOrObjectOrTrait, comments.leading(tree).filter(_.isScaladoc).toList) match {
        case (true, List(scaladocComment)) => Some(Documentable(maybePkg, tree, ScaladocParser.parseScaladoc(scaladocComment)))
        case (true, _)                     => Some(Documentable(maybePkg, tree, None))
        case _                             => None
      }

    def ext(tree: Tree): List[Documentable] =
      tree.children.foldRight(parsedScaladocComment(tree).toList) { (childTree, acc) =>
        acc ::: ext(childTree)
      }

    ext(tree)
  }

  private def sourceToMaybePkg(source: Source): Option[Pkg] =
    source.children.filter(_.isInstanceOf[Pkg]) match {
      case head :: Nil => Some(head.asInstanceOf[Pkg])
      case Nil => None
      case _ :: tail => throw new RuntimeException(tail.toString)
    }

  private def parse(input: Input): Source =
    input.parse[Source] match {
      case Parsed.Success(source) => source
      case e: Parsed.Error      => throw e.details
    }

  override def readAlias(sources: ScalaSources): TypeAliases = {
    val typeAliasList = sources.list().asScala.foldRight(Nil: List[TypeAlias]) { (scalaSource, acc) =>
      val input = Input.Stream(scalaSource.toInputStream, StandardCharsets.UTF_8)
      val source  = parse(input)
      val typeAliases = for {
        documentable <- extractFromTree(sourceToMaybePkg(source), source)
        alias <- documentable.maybeAlias
      } yield new TypeAlias(documentable.typeIdentifier, alias)
      typeAliases ::: acc
    }
    new TypeAliases(typeAliasList.asJava, Collections.emptyList())
  }

}
