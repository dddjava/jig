package org.dddjava.jig.infrastructure.asm;

import org.dddjava.jig.domain.model.data.members.instruction.*;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;
import org.dddjava.jig.domain.model.information.members.JigMethod;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import testing.TestSupport;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class AsmMethodVisitorInstructionTest {

    private static class SutClass {
        public void 自クラスメソッド参照メソッド() {
            Stream.of(1)
                    .map(this::メソッド参照で呼ばれるメソッド1)
                    .close();
        }

        private Character メソッド参照で呼ばれるメソッド1(long l) {
            return 'c';
        }

        public void 他クラスメソッド参照メソッド() {
            Stream.of(1)
                    .map(AnotherClass::メソッド参照で呼ばれるメソッド2)
                    .close();
        }

        public void lambda式メソッド() {
            Stream.of(1)
                    .map(i -> {
                        int variant = UUID.randomUUID().variant();
                        return Double.toString(variant);
                    })
                    .close();
        }

        public void lambda式の多段メソッド() {
            Supplier<Object> s = () -> {
                return UUID.randomUUID();
            };
            Consumer<Object> c1 = o -> {
                Supplier<Object> s1 = () -> {
                    return o.hashCode();
                };
            };
            Consumer<Object> c2 = o -> {
                Supplier<Object> s2 = () -> {
                    Objects.requireNonNull(o);
                    return Objects.requireNonNullElseGet(o, () -> "null".length());
                };
            };
        }

        public String 分岐メソッド(Object arg) {
            if (arg == null) {
                return "args is null";
            }
            if (arg instanceof String stringArg) {
                if (stringArg.isEmpty()) {
                    return "args is empty";
                } else if (stringArg.length() > 10) {
                    return "args is too long";
                }
            }
            return "args is ok";
        }

        String switchメソッド1(int i) {
            // 続いているものはtable switchになる
            return switch (i) {
                case 1 -> "1";
                case 2 -> "2";
                case 3 -> "3";
                default -> "default";
            };
        }

        String switchメソッド2(int i) {
            // 続いていないものはlookup switchになる
            return switch (i) {
                case 1 -> "1";
                case 3 -> "3";
                default -> "default";
            };
        }

        String tryCatchメソッド() {
            try {
                System.out.println("try-block");
                return "try-block";
            } catch (Exception e) {
                return "catch-block-exception";
            }
        }

        public String tryCatchネストメソッド() {
            try {
                try {
                    System.out.println("nest-try-block");
                } catch (RuntimeException e) {
                    System.out.println("nest-catch-block-runtime-exception");
                }
                return "try-block";
            } catch (RuntimeException e) {
                return "catch-block-runtime-exception";
            } catch (Exception e) {
                return "catch-block-exception";
            } finally {
                System.out.printf("finally-block%n");
            }
        }

        int instanceField;

        int フィールドアクセス() {
            return ++instanceField;
        }
    }

    private static class AnotherClass {
        static CharSequence メソッド参照で呼ばれるメソッド2(int i) {
            return "anotherMethod";
        }
    }

    @Test
    void 自身のメソッド参照のInstructionが検出できる() {
        JigMethod jigMethod = TestSupport.JigMethod準備(SutClass.class, "自クラスメソッド参照メソッド");

        MethodCall actual = getDynamicMethodCallStream(jigMethod)
                .findAny().orElseThrow();

        assertEquals("メソッド参照で呼ばれるメソッド1", actual.methodName());
    }

    private static Stream<MethodCall> getDynamicMethodCallStream(JigMethod jigMethod) {
        return jigMethod.instructions().instructions().stream()
                .filter(instruction -> instruction instanceof DynamicMethodCall || instruction instanceof LambdaExpressionCall)
                .flatMap(instruction -> instruction.findMethodCall());
    }

    @Test
    void 違うクラスのメソッド参照のInstructionが検出できる() {
        JigMethod jigMethod = TestSupport.JigMethod準備(SutClass.class, "他クラスメソッド参照メソッド");

        MethodCall actual = getDynamicMethodCallStream(jigMethod)
                .findAny().orElseThrow();

        assertEquals("メソッド参照で呼ばれるメソッド2", actual.methodName());
    }

    @Test
    void Lambda式のInstructionが検出できる() {
        JigMethod jigMethod = TestSupport.JigMethod準備(SutClass.class, "lambda式メソッド");

        MethodCall actual = getDynamicMethodCallStream(jigMethod)
                .findAny().orElseThrow();

        assertEquals("lambda$lambda式メソッド$0", actual.methodName());
    }

    @Test
    void Lambda式で呼び出しているメソッドがlambda式を記述したメソッドから取得できる() {
        JigMethod jigMethod = TestSupport.JigMethod準備(SutClass.class, "lambda式メソッド");

        List<String> actual = jigMethod.instructions().lambdaInlinedMethodCallStream()
                .map(MethodCall::methodName)
                // バイトコードの順番は記述順と異なるので名前順にしておく
                .sorted()
                .toList();

        assertEquals(Stream.of(
                "of", "map", "close", "valueOf", // lambda式の外（valueOfはオートボクシング）
                "randomUUID", "variant", "toString" // lambda式の内側
        ).sorted().toList(), actual);
    }

    @Test
    void Lambda式で呼び出しているメソッドがlambda式を記述したメソッドから取得できる_多段() {
        JigMethod jigMethod = TestSupport.JigMethod準備(SutClass.class, "lambda式の多段メソッド");

        List<String> actual = jigMethod.instructions().lambdaInlinedMethodCallStream()
                .map(MethodCall::methodName)
                // バイトコードの順番は記述順と異なるので名前順にしておく
                .sorted()
                .toList();

        assertEquals(Stream.of(
                "randomUUID", "hashCode", "requireNonNull", "requireNonNullElseGet", "length",
                "valueOf", "valueOf" // オートボクシング
        ).sorted().toList(), actual);
    }

    @CsvSource({
            // return, arg
            "自クラスメソッド参照メソッド, java.lang.Character",
            "自クラスメソッド参照メソッド, long",
            // owner, return, arg
            "他クラスメソッド参照メソッド, org.dddjava.jig.infrastructure.asm.AsmMethodVisitorInstructionTest$AnotherClass",
            "他クラスメソッド参照メソッド, java.lang.CharSequence",
            "他クラスメソッド参照メソッド, int",
            // return, arg, lambda内使用
            "lambda式メソッド, java.lang.String",
            "lambda式メソッド, java.lang.Integer",
            "lambda式メソッド, java.lang.Double",
            "lambda式メソッド, java.util.UUID",
            "lambda式メソッド, int",
    })
    @ParameterizedTest
    void メソッド参照やLambda式で使用している型が検出できる(String methodName, String expected) {
        JigMethod jigMethod = TestSupport.JigMethod準備(SutClass.class, methodName);

        Set<TypeIdentifier> actual = jigMethod.jigMethodDeclaration().associatedTypes();
        assertTrue(actual.contains(TypeIdentifier.valueOf(expected)), actual.toString());
    }

    @Test
    void 分岐メソッドからLabelが取得できる() {
        JigMethod jigMethod = TestSupport.JigMethod準備(SutClass.class, "分岐メソッド");

        var instructionList = jigMethod.instructions().instructions();

        List<JumpOrBranchInstruction> branchInstructions = instructionList.stream()
                .filter(instruction -> instruction instanceof JumpOrBranchInstruction)
                .map(instruction -> (JumpOrBranchInstruction) instruction)
                .toList();

        assertEquals(4, branchInstructions.size(), "分岐命令がifの数だけ存在する");

        var branchTargetInstructions = branchInstructions.stream().map(JumpOrBranchInstruction::target).toList();

        var targetInstructions = instructionList.stream()
                .filter(instruction -> instruction instanceof TargetInstruction)
                .map(instruction -> (TargetInstruction) instruction)
                .toList();

        assertTrue(branchTargetInstructions.containsAll(targetInstructions), "分岐命令のターゲットがすべて存在する");
    }

    @Test
    void tryCatchブロックが取得できる() {
        JigMethod jigMethod = TestSupport.JigMethod準備(SutClass.class, "tryCatchメソッド");

        assertTrue(
                jigMethod.instructions().instructions().stream()
                        .anyMatch(instruction -> instruction instanceof TryCatchInstruction)
        );
    }

    @Test
    void switchが取得できる1() {
        JigMethod jigMethod = TestSupport.JigMethod準備(SutClass.class, "switchメソッド1");

        assertTrue(
                jigMethod.instructions().instructions().stream()
                        .anyMatch(instruction -> {
                            if (instruction instanceof BasicInstruction bi) {
                                return bi.isBranch();
                            }
                            return false;
                        })
        );
    }

    @Test
    void switchが取得できる2() {
        JigMethod jigMethod = TestSupport.JigMethod準備(SutClass.class, "switchメソッド2");

        assertTrue(
                jigMethod.instructions().instructions().stream()
                        .anyMatch(instruction -> {
                            if (instruction instanceof BasicInstruction bi) {
                                return bi.isBranch();
                            }
                            return false;
                        })
        );
    }

    @Test
    void フィールドアクセスが取得できる() {
        JigMethod jigMethod = TestSupport.JigMethod準備(SutClass.class, "フィールドアクセス");

        List<Instruction> actual = jigMethod.instructions().instructions();
        assertTrue(
                actual.stream().anyMatch(instruction -> instruction instanceof FieldAccess),
                "フィールドアクセス命令が含まれること"
        );

        Map<String, Long> collect = actual.stream()
                .filter(instruction -> instruction instanceof FieldAccess)
                .collect(Collectors.groupingBy(
                        instruction -> instruction.getClass().getSimpleName(),
                        Collectors.counting()));

        assertAll(
                () -> assertEquals(1, collect.get("GetAccess")),
                () -> assertEquals(1, collect.get("SetAccess"))
        );
    }
}
