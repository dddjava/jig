package org.dddjava.jig.infrastructure.logger;

import org.dddjava.jig.domain.model.jigdocument.JigLogger;
import org.dddjava.jig.domain.model.jigdocument.Warning;
import org.dddjava.jig.infrastructure.resourcebundle.Utf8ResourceBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ResourceBundle;

public class MessageLogger implements org.dddjava.jig.domain.model.jigdocument.JigLogger {

    private final Class<?> clz;
    private final Logger logger;

    public MessageLogger(Class<?> clz) {
        this.clz = clz;
        this.logger = LoggerFactory.getLogger(clz);
    }

    public static JigLogger of(Class<?> clz) {
        return new MessageLogger(clz);
    }

    @Override
    public void warn(Warning warning) {
        ResourceBundle resource = Utf8ResourceBundle.messageBundle();
        String message = resource.getString(warning.resourceKey());
        logger.warn(message);
    }
}
