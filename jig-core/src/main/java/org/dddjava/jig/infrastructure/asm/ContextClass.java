package org.dddjava.jig.infrastructure.asm;

import org.dddjava.jig.domain.model.data.members.fields.JigFieldHeader;
import org.dddjava.jig.domain.model.data.members.instruction.Instruction;
import org.dddjava.jig.domain.model.data.members.methods.JigMethodHeader;
import org.dddjava.jig.domain.model.data.types.TypeId;

import java.util.List;

/**
 * メンバのVisitorから使用してよいメソッドを限定するためのインタフェース
 */
public interface ContextClass {

    int api();

    TypeId typeId();

    boolean isEnum();

    boolean isRecord();

    void addJigFieldHeader(JigFieldHeader jigFieldHeader);

    void finishVisitMethod(JigMethodHeader jigMethodHeader, List<Instruction> methodInstructionList);

    boolean isRecordComponentName(String name);
}
