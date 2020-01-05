package org.dddjava.jig.infrastructure

import java.nio.charset.StandardCharsets
import java.util.Collections

import org.dddjava.jig.domain.model.declaration.`type`.TypeIdentifier
import org.dddjava.jig.domain.model.jigloaded.alias.{
  Alias,
  JavadocAliasSource,
  MethodAlias,
  ScalaSourceAliasReader,
  TypeAlias,
  TypeAliases
}
import org.dddjava.jig.domain.model.jigsource.source.code.scalacode.ScalaSources

import scala.jdk.CollectionConverters._
import scala.meta._
import scala.meta.contrib._

class ScalametaAliasReader() extends ScalaSourceAliasReader {

  case class TypeIdentifierCandidate(parent: Option[TypeIdentifierCandidate], tree: Tree)

  case class Documentable(
      parent: Option[TypeIdentifierCandidate],
      tree: Tree,
      doxText: Option[List[DocToken]]
  ) {

    @scala.annotation.tailrec
    private def recursiveGetName(acc: List[Tree], maybe: Option[TypeIdentifierCandidate]): Option[String] =
      maybe match {
        case Some(c) => recursiveGetName(acc :+ c.tree, c.parent)
        case None =>
          acc match {
            case Nil => None
            case l =>
              Some(
                l.map(_.packageString).reverse.mkString("")
              )
          }
      }

    private lazy val packageName: String    = recursiveGetName(Nil, parent).getOrElse("")
    private lazy val fullName: String       = s"$packageName${tree.name}"
    lazy val typeIdentifier: TypeIdentifier = new TypeIdentifier(fullName)
    lazy val maybeAlias: Option[Alias] = doxText match {
      case Some(DocToken(DocToken.Description, Some(name), _) :: _) => Some(new JavadocAliasSource(name).toAlias)
      case Some(DocToken(DocToken.Description, _, Some(body)) :: _) => Some(new JavadocAliasSource(body).toAlias)
      case _                                                        => None
    }
  }

  implicit class RichTree(tree: Tree) {

    lazy val isTypeIdentifierCandidates: Boolean = tree match {
      case _: Pkg         => true
      case _: Pkg.Object  => true
      case _: Defn.Class  => true
      case _: Defn.Object => true
      case _: Defn.Trait  => true
      case _              => false
    }

    lazy val name: String = tree match {
      case p: Pkg         => p.ref.syntax
      case p: Pkg.Object  => p.name.value
      case d: Defn.Class  => d.name.value
      case d: Defn.Object => s"${d.name.value}$$"
      case d: Defn.Trait  => d.name.value
      case _              => ""
    }

    lazy val packageString: String = tree match {
      case p: Pkg         => s"${p.ref.syntax}."
      case p: Pkg.Object  => s"${p.name.value}.package$$"
      case d: Defn.Class  => d.name.value
      case d: Defn.Object => s"${d.name.value}$$"
      case d: Defn.Trait  => d.name.value
      case _              => ""
    }
  }

  private def extractFromTree(tree: Tree): List[Documentable] = {
    val comments = AssociatedComments(tree)

    def generate(parent: Option[TypeIdentifierCandidate], tree: Tree): Option[TypeIdentifierCandidate] =
      if (tree.isTypeIdentifierCandidates) Some(TypeIdentifierCandidate(parent, tree)) else parent

    def parsedScaladocComment(parent: Option[TypeIdentifierCandidate], tree: Tree): Option[Documentable] =
      (tree.isTypeIdentifierCandidates, comments.leading(tree).filter(_.isScaladoc).toList) match {
        case (true, List(scaladocComment)) =>
          Some(Documentable(parent, tree, ScaladocParser.parseScaladoc(scaladocComment)))
        case (true, _) => Some(Documentable(parent, tree, None))
        case _         => None
      }

    def ext(maybe: Option[TypeIdentifierCandidate], tree: Tree): List[Documentable] = {
      val parent = generate(maybe, tree)
      tree.children.foldRight(parsedScaladocComment(maybe, tree).toList) { (childTree, acc) =>
        acc ::: ext(parent, childTree)
      }
    }

    ext(None, tree)
  }

  private def parse(input: Input): Source =
    input.parse[Source] match {
      case Parsed.Success(source) => source
      case e: Parsed.Error        => throw e.details
    }

  override def readAlias(sources: ScalaSources): TypeAliases = {
    val (typeAliasList, methodAliasList) =
      sources.list().asScala.foldRight((Nil: List[TypeAlias], Nil: List[MethodAlias])) {
        case (scalaSource, (acc1, acc2)) =>
          val input         = Input.Stream(scalaSource.toInputStream, StandardCharsets.UTF_8)
          val source        = parse(input)
          val documentables = extractFromTree(source)
          val a = for {
            documentable <- documentables
            alias        <- documentable.maybeAlias
          } yield new TypeAlias(documentable.typeIdentifier, alias)

          (acc1 ::: a, acc2)
      }
    new TypeAliases(typeAliasList.asJava, methodAliasList.asJava)
  }

}
