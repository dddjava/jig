package jig.classlist;

import jig.classlist.infrastructure.writer.ExcelWriter;
import jig.classlist.infrastructure.writer.TsvWriter;

public enum ReportFormat {
    TSV {
        @Override
        public ReportWriter writer() {
            return new TsvWriter();
        }
    },
    EXCEL {
        @Override
        public ReportWriter writer() {
            return new ExcelWriter();
        }
    };

    public abstract ReportWriter writer();

    public static ReportFormat from(String outputPath) {
        if (outputPath.endsWith(".xlsx")) return EXCEL;
        return TSV;
    }
}
