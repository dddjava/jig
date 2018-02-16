package jig.domain.model;

import javax.xml.bind.DatatypeConverter;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class DiagramSource {

    private final String value;

    public DiagramSource(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public DiagramIdentifier getIdentifier() {
        try {
            MessageDigest msdDigest = MessageDigest.getInstance("SHA-1");
            msdDigest.update(value.getBytes("UTF-8"), 0, value.length());
            String sha1 = DatatypeConverter.printHexBinary(msdDigest.digest());
            return new DiagramIdentifier(sha1);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            throw new IllegalStateException();
        }
    }
}
