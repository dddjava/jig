package org.dddjava.jig.domain.model.data.members.instruction;

/**
 * 分岐
 *
 * ```
 *   ifeq, ifne, ifle, iflt, ifge, ifgt,
 *   ifnull, ifnonnull,
 *   if_icmpeq, if_icmpne, if_icmple, if_icmplt, if_icmpge, if_icmpgt,
 *   if_acmpeq, if_acmpne
 * ```
 *
 * `jsr, jsr_w, goto, goto_w` は jump or branch instructionで括られるが、これには含まない。
 * switchはtargetが複数ある分岐なので別で扱う。
 */
public record IfInstruction(Kind kind, JumpTarget target) implements Instruction {

    public enum Kind {
        比較,
        NULL判定,
        UNKNOWN
    }

    public static Instruction from(Kind kind, String targetId) {
        return new IfInstruction(kind, new JumpTarget(targetId));
    }
}
