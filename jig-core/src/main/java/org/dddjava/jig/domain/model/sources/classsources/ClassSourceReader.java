package org.dddjava.jig.domain.model.sources.classsources;

/**
 * 対象から実装を取得するファクトリ
 */
public interface ClassSourceReader {

    ClassSourceModel classSourceModel(ClassSources classSources);
}
