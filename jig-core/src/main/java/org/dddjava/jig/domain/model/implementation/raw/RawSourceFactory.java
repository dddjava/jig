package org.dddjava.jig.domain.model.implementation.raw;

/**
 * 生ソース読み取り機
 */
public interface RawSourceFactory {

    RawSource createSource(RawSourceLocations rawSourceLocations);
}
