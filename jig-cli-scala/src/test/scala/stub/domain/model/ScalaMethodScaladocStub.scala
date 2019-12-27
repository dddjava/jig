package stub.domain.model

import java.time.LocalDateTime

class ScalaMethodScaladocStub {

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

object ScalaMethodScaladocStub {

  /**
   * コンパニオンオブジェクトのメソッド
   */
  def companionObjectMethod(): Unit = ???

}
