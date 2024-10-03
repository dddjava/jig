package org.dddjava.jig.infrastructure.view.poi.report;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.dddjava.jig.domain.model.documents.stationery.JigDocumentContext;
import org.dddjava.jig.domain.model.models.domains.businessrules.BusinessRules;
import org.dddjava.jig.domain.model.models.jigobject.class_.JigType;
import org.dddjava.jig.domain.model.models.jigobject.member.JigMethod;
import org.dddjava.jig.domain.model.parts.classes.field.FieldDeclarations;
import org.dddjava.jig.domain.model.parts.classes.method.CallerMethods;
import org.dddjava.jig.domain.model.parts.classes.method.MethodDeclaration;
import org.dddjava.jig.domain.model.parts.classes.method.MethodDeclarations;
import org.dddjava.jig.domain.model.parts.classes.method.UsingFields;
import org.dddjava.jig.domain.model.parts.classes.type.ClassComment;
import org.dddjava.jig.domain.model.parts.classes.type.TypeDeclaration;
import org.dddjava.jig.domain.model.parts.classes.type.TypeIdentifier;
import org.dddjava.jig.domain.model.parts.classes.type.TypeIdentifiers;
import org.dddjava.jig.domain.model.parts.packages.PackageIdentifier;
import org.dddjava.jig.infrastructure.view.report.ReportItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * 一覧出力項目のフォーマッター
 */
public class ReportItemFormatter {
    static Logger logger = LoggerFactory.getLogger(ReportItemFormatter.class);

    private final JigDocumentContext jigDocumentContext;

    public ReportItemFormatter(JigDocumentContext jigDocumentContext) {
        this.jigDocumentContext = jigDocumentContext;
    }

    void format(ReportItem reportItem, Object item, Cell cell) {
        switch (reportItem) {
            case パッケージ名:
                cell.setCellValue(toPackageIdentifier(item).asText());
                return;
            case パッケージ別名:
                cell.setCellValue(jigDocumentContext.packageComment(toPackageIdentifier(item)).asText());
                return;
            case クラス名:
            case 単純クラス名:
                cell.setCellValue(toTypeIdentifier(item).asSimpleText());
                return;
            case クラス別名:
                cell.setCellValue(jigDocumentContext.classComment(toTypeIdentifier(item)).asText());
                return;
            case メソッドシグネチャ:
                cell.setCellValue(toMethodDeclaration(item).asSignatureSimpleText());
                return;
            case メソッド別名:
                cell.setCellValue(toMethod(item).aliasTextOrBlank());
                return;
            case メソッド戻り値の型:
                cell.setCellValue(toMethodDeclaration(item).methodReturn().asSimpleText());
                return;
            case メソッド戻り値の型の別名:
                cell.setCellValue(jigDocumentContext.classComment(toMethodDeclaration(item).methodReturn().typeIdentifier()).asText());
                return;
            case メソッド引数の型の別名: {
                MethodDeclaration methodDeclaration = toMethodDeclaration(item);
                List<ClassComment> list = methodDeclaration.methodSignature().arguments().stream()
                        .map(jigDocumentContext::classComment)
                        .collect(toList());
                writeLongString(cell, list.stream()
                        .map(alias -> alias.asText())
                        .collect(Collectors.joining(", ", "[", "]")));
            }
            return;
            case 使用しているフィールドの型:
                writeLongString(cell, ((UsingFields) item).typeIdentifiers().asSimpleText());
                return;
            case フィールドの型: {
                // TODO: onlyOne複数に対応する。型引数を出力したいのでFieldTypeを使用している。
                FieldDeclarations fieldDeclarations = ((JigType) item).instanceMember().fieldDeclarations();
                String result = fieldDeclarations.onlyOneField().fieldType().asSimpleText();
                writeLongString(cell, result);
            }
            return;
            case 使用箇所数:
                if (item instanceof CallerMethods) {
                    cell.setCellValue(((CallerMethods) item).size());
                } else {
                    cell.setCellValue(((TypeIdentifiers) item).size());
                }
                return;
            case 使用箇所:
                writeLongString(cell, ((TypeIdentifiers) item).asSimpleText());
                return;
            case クラス数:
                cell.setCellValue(((BusinessRules) item).list().size());
                return;
            case メソッド数:
                cell.setCellValue((toMethodDeclarations(item)).number().intValue());
                return;
            case メソッド一覧: {
                writeLongString(cell, toMethodDeclarations(item).asSignatureAndReturnTypeSimpleText());
            }
            return;
            case 分岐数:
                cell.setCellValue(((JigMethod) item).decisionNumber().intValue());
                return;
            case 汎用文字列:
                cell.setCellValue((String) item);
                return;
            case イベントハンドラ:
            case 汎用真偽値:
                cell.setCellValue(((Boolean) item) ? "◯" : "");
                return;
            case 汎用数値:
                cell.setCellValue((int) item);
                return;
        }

        throw new IllegalArgumentException(reportItem.name());
    }

    private JigMethod toMethod(Object item) {
        return (JigMethod) item;
    }

    private MethodDeclarations toMethodDeclarations(Object item) {
        if (item instanceof JigType) {
            return ((JigType) item).instanceMember().instanceMethods().declarations();
        }
        return (MethodDeclarations) item;
    }

    private PackageIdentifier toPackageIdentifier(Object item) {
        if (item instanceof PackageIdentifier) {
            return (PackageIdentifier) item;
        }
        return toTypeIdentifier(item).packageIdentifier();
    }

    private TypeIdentifier toTypeIdentifier(Object item) {
        if (item instanceof TypeIdentifier) {
            return (TypeIdentifier) item;
        }
        if (item instanceof TypeDeclaration) {
            return ((TypeDeclaration) item).identifier();
        }
        if (item instanceof JigType) {
            return ((JigType) item).identifier();
        }

        MethodDeclaration methodDeclaration = toMethodDeclaration(item);
        return methodDeclaration.declaringType();
    }

    private MethodDeclaration toMethodDeclaration(Object item) {
        if (item instanceof JigMethod) {
            return ((JigMethod) item).declaration();
        }
        return (MethodDeclaration) item;
    }

    public void apply(Row row, ReportItemMethod reportItemMethod, Object methodReturnValue) {
        short lastCellNum = row.getLastCellNum();
        Cell cell = row.createCell(lastCellNum == -1 ? 0 : lastCellNum);

        format(reportItemMethod.value(), methodReturnValue, cell);
    }

    private void writeLongString(Cell cell, String result) {
        String value = result;
        if (result.length() > 10000) {
            logger.info("セル(row={}, column={})に出力する文字数が10,000文字を超えています。全ての文字は出力されません。",
                    cell.getRowIndex(), cell.getColumnIndex());
            value = result.substring(0, 10000) + "...(省略されました）";
        }
        cell.setCellValue(value);
    }
}
