package org.dddjava.jig.domain.model.implementation.raw;

import java.nio.file.Path;
import java.util.List;

public class RawSourceLocations {

    BinarySourceLocations binarySourceLocations;
    TextSourceLocations textSourceLocations;

    public RawSourceLocations(BinarySourceLocations binarySourceLocations, TextSourceLocations textSourceLocations) {
        this.binarySourceLocations = binarySourceLocations;
        this.textSourceLocations = textSourceLocations;
    }

    public List<Path> binarySourcePaths() {
        return binarySourceLocations.paths();
    }

    public List<Path> textSourcePaths() {
        return textSourceLocations.paths();
    }

    public RawSourceLocations merge(RawSourceLocations other) {
        return new RawSourceLocations(binarySourceLocations.merge(other.binarySourceLocations), textSourceLocations.merge(other.textSourceLocations));
    }
}
