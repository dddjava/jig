package stub.domain.model

import java.time.LocalDateTime

class KotlinMethodJavadocStub {

    /**
     * メソッドのドキュメント
     */
    fun simpleMethod() {

    }

    /**
     * 引数なしのメソッド
     */
    fun overloadMethod(): String? {
        return null
    }

    /**
     * 引数ありのメソッド
     */
    fun overloadMethod(str: String, dateTime: LocalDateTime): String? {
        return null
    }
}

/**
 * トップレベルのメソッド
 */
fun topLevel() {

}