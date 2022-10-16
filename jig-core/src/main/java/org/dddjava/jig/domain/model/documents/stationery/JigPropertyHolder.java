package org.dddjava.jig.domain.model.documents.stationery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class JigPropertyHolder {
    static Logger logger = LoggerFactory.getLogger(JigPropertyHolder.class);
    private static final JigPropertyHolder INSTANCE = new JigPropertyHolder();

    public static JigPropertyHolder getInstance() {
        return INSTANCE;
    }

    private JigPropertyHolder() {
    }

    private final Map<String, String> map = new ConcurrentHashMap<>();

    public void load(Properties input) {
        input.forEach((key, value) -> map.put(String.valueOf(key), String.valueOf(value)));
        logger.info("loaded property {}", input);
    }

    public Optional<String> get(String key) {
        return Optional.ofNullable(map.get(key));
    }
}
