package org.dddjava.jig.domain.model.jigmodel.smells;

import org.dddjava.jig.domain.model.jigloaded.richmethod.Method;

import java.util.List;

/**
 * 文字列比較を行なっているメソッド
 *
 * 文字列比較を行なっているメソッドはビジネスルールの分類判定を行なっている可能性が高い。
 * サービスなどに登場した場合はかなり拙いし、そうでなくても列挙を使用するなど改善の余地がある。
 */
public class StringComparingCallerMethods {

    List<Method> methods;

    public StringComparingCallerMethods(List<Method> methods) {
        this.methods = methods;
    }

    public List<Method> list() {
        return methods;
    }
}
