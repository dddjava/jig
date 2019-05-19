package stub.domain.model

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
    fun overloadMethod(str: String): String? {
        return null
    }
}

/**
 * トップレベルのメソッド
 */
fun topLevel() {

}