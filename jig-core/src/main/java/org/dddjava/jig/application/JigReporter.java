package org.dddjava.jig.application;

import org.dddjava.jig.domain.model.documents.stationery.Warning;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.Set;

/**
 * 生成における情報を記録する
 */
@Repository
public class JigReporter {
    private static final Logger logger = LoggerFactory.getLogger(JigReporter.class);

    private final Set<Warning> warnings = new HashSet<>();

    public void エントリーポイントが見つからないので一部の情報が出力されない() {
        warnings.add(Warning.ハンドラメソッドが見つからないので出力されない通知);
    }

    public void notifyWithLogger() {
        warnings.stream().map(Warning::localizedMessage).forEach(logger::warn);
    }
}
