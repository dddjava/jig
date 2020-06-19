package org.dddjava.jig.presentation.view.poi.report;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.richmethod.MethodWorry;
import org.dddjava.jig.presentation.view.poi.report.formatter.ReportItemFormatters;
import org.dddjava.jig.presentation.view.report.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class ModelReport<MODEL> {

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

    public void writeSheet(Workbook book, ReportItemFormatters reportItemFormatters) {
        Sheet sheet = book.createSheet(title);
        writeHeader(sheet);
        writeBody(sheet, reportItemFormatters);
        updateSheetAttribute(sheet);
    }

    void writeHeader(Sheet sheet) {
        Header header = header();
        for (int i = 0; i < header.size(); i++) {
            // headerは全てSTRINGで作る
            Cell cell = sheet.createRow(0).createCell(i, CellType.STRING);
            cell.setCellValue(header.textOf(i));
        }
    }

    void writeBody(Sheet sheet, ReportItemFormatters reportItemFormatters) {
        for (MODEL pivotModel : pivotModels) {
            List<String> convertedRow = new ArrayList<>();
            for (ReportItemMethod reportItemMethod : reportItemMethods) {
                try {
                    Object report = modelReporter.report(pivotModel);
                    Object item = reportItemMethod.method.invoke(report);
                    String result = reportItemFormatters.format(reportItemMethod.reportItemFor.value(), item);
                    convertedRow.add(result);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException("実装ミス", e);
                }
            }
            ReportRow row = new ReportRow(convertedRow);
            row.writeRow(sheet.createRow(sheet.getLastRowNum() + 1));
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
