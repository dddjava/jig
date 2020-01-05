package stub.domain.model

import java.time.LocalDateTime

/**
  * ScalaのTraitのDoc
  */
trait ScalaMethodScaladocStub {

  /**
    * メソッドのドキュメント
    */
  def simpleMethod(): Unit = ???

  /**
    * 引数なしのメソッド
    */
  def overloadMethod(): Option[String] = ???

  /**
    * 引数ありのメソッド
    */
  def overloadMethod(str: String, dateTime: LocalDateTime): Option[String] = ???

}

/**
  * ScalaのObjectのDoc
  */
object ScalaMethodScaladocStub {

  /**
    * コンパニオンオブジェクトのメソッド
    */
  def companionObjectMethod(): Unit = ???

  /**
    * Object内のTrait
    */
  sealed trait SealedTrait

  /** SealedされたCaseObjectの1 */
  case object SealedCaseObject1 extends SealedTrait

  /** SealedされたCaseObjectの2 */
  case object SealedCaseObject2 extends SealedTrait

  /**
   * Objectの中のObject
   */
  object ObjectInObject {

    /**
     * Objectの中のObjectの中のObject
     */
    case class ObjectInObjectInObject()
  }
}
