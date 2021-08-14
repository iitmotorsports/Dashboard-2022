package com.iit.dashboard2022.ui;

import android.os.Handler;
import android.os.Looper;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class UITester {
    private static final Set<TestUI> UITests = new HashSet<>();
    private static final Handler UIHandle = new Handler(Looper.getMainLooper());
    public static final Random Rnd = new Random();
    private static final int UI_UPDATE_MS = 20;
    private static final float UI_UPDATE_VAL = 0.005f;
    private static final int rndTests = (int) (1.0f / UI_UPDATE_VAL);
    private static int rndTestC = rndTests;
    private static boolean rndTest = false;
    private static float testVal = 0;

    public interface TestUI {
        void testUI(float percent);
    }

    @SuppressWarnings("SpellCheckingInspection")
    private static final String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ\n\t";

    public static String rndStr(int count) {
        char[] text = new char[count];
        for (int i = 0; i < count; i++) {
            text[i] = chars.charAt(Rnd.nextInt(chars.length()));
        }
        return new String(text);
    }

    private static void runTest(float val) {
        for (TestUI test : UITests) {
            test.testUI(val);
        }
    }

    private static void uiTest() {
        runTest(testVal);

        if (rndTest || testVal >= 1.2) {
            testVal = Rnd.nextFloat();
            rndTest = true;
            rndTestC--;
            if (rndTestC == 0) {
                rndTestC = rndTests;
                rndTest = false;
                testVal = 0;
            }
        } else {
            testVal += UI_UPDATE_VAL;
        }

        UIHandle.postDelayed(UITester::uiTest, UI_UPDATE_MS);
    }

    public static void enable(boolean enabled) {
        if (enabled) {
            rndTestC = rndTests;
            rndTest = false;
            testVal = 0;
            UIHandle.post(UITester::uiTest);
        } else {
            UIHandle.removeCallbacksAndMessages(null);
            runTest(0);
        }
    }

    public static void addTest(TestUI test) {
        UITests.add(test);
    }

    public static void removeTest(TestUI test) {
        UITests.remove(test);
    }

}
