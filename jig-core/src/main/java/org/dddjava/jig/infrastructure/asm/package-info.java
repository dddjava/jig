/**
 * ASMを使用してクラスファイルを読む
 *
 * クラスファイルの解析はJIGの根幹であるが、ASMを前提にするものではない。
 * JEP-457 https://openjdk.org/jeps/457 が正式に採用されたら置き換えるかもしれないので、
 * このパッケージ外にはASMを出さないようにする。
 */
package org.dddjava.jig.infrastructure.asm;