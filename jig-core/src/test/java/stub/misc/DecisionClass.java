package stub.misc;

import java.util.Collections;

/**
 * 分岐を検出するためのクラス
 */
public class DecisionClass {
    int condition;

    void 分岐なしメソッド() {
    }

    void ifがあるメソッド() {
        if (condition <= 0) {
            System.out.println("true");
        }
        System.out.println("hoge");
    }

    void switchがあるメソッド() {
        switch (condition) {
            case 1:
                System.out.println(1);
            case 2:
                System.out.println(2);
            default:
                System.out.println(0);
        }
    }

    void forがあるメソッド() {
        for (Object obj : Collections.emptyList()) {
            System.out.println(obj);
        }
    }
}
