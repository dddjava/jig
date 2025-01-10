package org.dddjava.jig.adapter;

/**
 * Adapterのインタフェース。
 * このインタフェースを実装したクラスの {@link HandleDocument} メソッドを呼び出してwriterに引き渡す。
 *
 * @param <T> writerが処理する型。通常はメソッドの戻り値もこの型になります。
 */
public interface Adapter<T> {

    /**
     * Adapterの処理結果を書き出せる形に変換します。
     * Adapterで実装されているハンドラメソッドの結果がすべてwriterが扱えるなら実装不要。
     */
    @SuppressWarnings("unchecked")
    default T convertMethodResultToAdapterModel(Object resultModel) {
        return (T) resultModel;
    }
}
