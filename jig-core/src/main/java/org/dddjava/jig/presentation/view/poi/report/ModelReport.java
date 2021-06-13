package org.dddjava.jig.presentation.view.poi.report;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.dddjava.jig.domain.model.models.jigobject.member.MethodWorry;
import org.dddjava.jig.presentation.view.report.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class ModelReport<MODEL> {

    static Logger logger = LoggerFactory.getLogger(ModelReport.class);

    String title;
    List<MODEL> pivotModels;
    ModelReporter<?, MODEL> modelReporter;
    List<ReportItemMethod> reportItemMethods;

    public <REPORT> ModelReport(List<MODEL> pivotModels, ModelReporter<REPORT, MODEL> modelReporter, Class<REPORT> reportClass) {
        this(reportClass.getAnnotation(ReportTitle.class).value(), pivotModels, modelReporter, reportClass);
    }

    public <REPORT> ModelReport(String title, List<MODEL> pivotModels, ModelReporter<REPORT, MODEL> modelReporter, Class<REPORT> reportClass) {
        this(title, pivotModels, modelReporter, collectReportItemMethods(reportClass));
    }

    private static <REPORT> List<ReportItemMethod> collectReportItemMethods(Class<REPORT> reportClass) {
        // @ReportItemForを収集
        Stream<ReportItemMethod> items = Arrays.stream(reportClass.getMethods())
                .filter(method -> method.isAnnotationPresent(ReportItemFor.class) || method.isAnnotationPresent(ReportItemsFor.class))
                .flatMap(method -> {
                    // 複数アノテーションがついていたら展開
                    if (method.isAnnotationPresent(ReportItemsFor.class)) {
                        return Arrays.stream(method.getAnnotation(ReportItemsFor.class).value())
                                .map(reportItemFor -> new ReportItemMethod(method, reportItemFor));
                    }

                    // 1つだけのはそのまま
                    ReportItemFor reportItemFor = method.getAnnotation(ReportItemFor.class);
                    return Stream.of(new ReportItemMethod(method, reportItemFor));
                });

        // @ReportMethodWorryOfを追加
        Stream<ReportItemMethod> methodWorries = Arrays.stream(reportClass.getMethods())
                .filter(method -> method.isAnnotationPresent(ReportMethodWorryOf.class))
                .map(method -> new ReportItemMethod(method, generateReportItemForInstance(method)));

        return Stream.concat(items, methodWorries)
                .sorted()
                .collect(toList());
    }

    private static ReportItemFor generateReportItemForInstance(Method method) {
        ReportMethodWorryOf reportMethodWorryOf = method.getAnnotation(ReportMethodWorryOf.class);
        MethodWorry value = reportMethodWorryOf.value();
        return new ReportItemFor() {

            @Override
            public Class<? extends Annotation> annotationType() {
                return ReportItemFor.class;
            }

            @Override
            public ReportItem value() {
                return ReportItem.汎用真偽値;
            }

            @Override
            public int order() {
                // とりあえず後ろにしておく
                return 100 + value.ordinal();
            }

            @Override
            public String label() {
                return value.toString();
            }
        };
    }

    private ModelReport(String title, List<MODEL> pivotModels, ModelReporter<?, MODEL> modelReporter, List<ReportItemMethod> reportItemMethods) {
        this.title = title;
        this.pivotModels = pivotModels;
        this.modelReporter = modelReporter;
        this.reportItemMethods = reportItemMethods;
    }

    Header header() {
        return new Header(reportItemMethods);
    }

    public void writeSheet(Workbook book, ReportItemFormatter reportItemFormatter) {
        if (pivotModels.isEmpty()) {
            logger.info("出力対象がないため {} シートをスキップしました。", title);
            return;
        }
        Sheet sheet = book.createSheet(title);
        writeHeader(sheet, reportItemFormatter);
        writeBody(sheet, reportItemFormatter);
        updateSheetAttribute(sheet);
    }

    void writeHeader(Sheet sheet, ReportItemFormatter reportItemFormatter) {
        Header header = header();
        Row row = sheet.createRow(0);
        for (int i = 0; i < header.size(); i++) {
            // headerは全てSTRINGで作る
            Cell cell = row.createCell(i, CellType.STRING);
            cell.setCellValue(header.textOf(i, reportItemFormatter));
        }
    }

    void writeBody(Sheet sheet, ReportItemFormatter reportItemFormatter) {
        for (MODEL pivotModel : pivotModels) {
            Row row = sheet.createRow(sheet.getLastRowNum() + 1);
            Object report = modelReporter.report(pivotModel);

            for (ReportItemMethod reportItemMethod : reportItemMethods) {
                Object methodReturnValue = reportItemMethod.invoke(report);
                reportItemFormatter.apply(row, reportItemMethod, methodReturnValue);
            }
        }
    }

    void updateSheetAttribute(Sheet sheet) {
        int columns = header().size();
        for (int i = 0; i < columns; i++) {
            // 列幅を自動調整する
            sheet.autoSizeColumn(i);
        }
        // オートフィルターを有効にする
        sheet.setAutoFilter(new CellRangeAddress(
                0, sheet.getLastRowNum(),
                0, columns - 1
        ));
    }
}
