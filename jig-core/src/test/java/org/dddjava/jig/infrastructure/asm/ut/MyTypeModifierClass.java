package org.dddjava.jig.infrastructure.asm.ut;

public class MyTypeModifierClass {
    public static class MyTypeModifierClassSTATIC {
        public static class MyTypeModifierClassSTATICNest {
        }

        public class MyTypeModifierClassSTATICInner {
        }
    }

    public abstract class MyTypeModifierClassABSTRACT {
    }

    public final class MyTypeModifierClassFINAL {
    }

    public sealed interface MyTypeModifierClassSEALED
            permits MyTypeModifierClassNON_SEALED {
    }

    public non-sealed class MyTypeModifierClassNON_SEALED implements MyTypeModifierClassSEALED {
    }
}
