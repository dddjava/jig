package jig.classlist.report;

import jig.classlist.report.method.MethodDetail;
import jig.classlist.report.method.MethodPerspective;
import jig.classlist.report.method.MethodReport;
import jig.classlist.report.type.TypeDetail;
import jig.classlist.report.type.TypePerspective;
import jig.classlist.report.type.TypeReport;
import jig.domain.model.tag.Tag;

import java.util.List;

public enum ReportPerspective {
    METHOD {
        @Override
        public Report getReport(Tag tag, ReportService reportService) {
            List<MethodDetail> details = reportService.methodDetails(tag);
            return new MethodReport(MethodPerspective.from(tag), details);
        }
    },
    TYPE {
        @Override
        public Report getReport(Tag tag, ReportService reportService) {
            List<TypeDetail> details = reportService.typeDetails(tag);
            return new TypeReport(TypePerspective.from(tag), details);
        }
    };

    public static ReportPerspective from(Tag tag) {
        if (tag == Tag.SERVICE || tag == Tag.REPOSITORY) {
            return METHOD;
        }
        return TYPE;
    }

    public abstract Report getReport(Tag tag, ReportService reportService);
}
