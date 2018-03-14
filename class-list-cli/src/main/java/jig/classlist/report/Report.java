package jig.classlist.report;

import java.util.List;

public interface Report {

    List<String> headerLabel();

    List<List<String>> rowList();
}
