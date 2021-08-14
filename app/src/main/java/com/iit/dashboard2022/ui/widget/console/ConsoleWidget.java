package com.iit.dashboard2022.ui.widget.console;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.text.PrecomputedTextCompat;

import com.iit.dashboard2022.R;
import com.iit.dashboard2022.ui.UITester;

import java.util.Locale;
import java.util.concurrent.ConcurrentLinkedQueue;


public class ConsoleWidget extends ConstraintLayout implements UITester.TestUI {

    private static final ConcurrentLinkedQueue<CharSequence> rawQueue = new ConcurrentLinkedQueue<>();
    private static final ConcurrentLinkedQueue<CharSequence> outQueue = new ConcurrentLinkedQueue<>();
    private static final HandlerThread consoleThread = new HandlerThread("Console Thread");
    private static final Handler uiHandle = new Handler(Looper.getMainLooper());
    private static Handler textHandle;

    private static final int UPDATE_TIME_MS = 100;
    private static long lastTime = 0;

    private PrecomputedTextCompat.Params textParams;
    private boolean run = false;

    private final ConsoleScroller consoleScroller;
    private final ImageView scrollToEndImage;
    private final String linesFormat, errorFormat, statusFormat, modeFormat;
    private final TextView text, consoleLines, consoleError, consoleStatus, consoleMode;
    private Status currentStatus = Status.Disconnected;
    private int errorCounter = 0;

    private final Runnable textUpdate = new Runnable() {
        @Override
        public void run() {
            CharSequence msg;

            if (text.getLineCount() > 1000) {
                CharSequence cq = text.getText();
                int len = cq.length();
                text.setText(cq.subSequence(len / 2, len));
            }

            while ((msg = outQueue.poll()) != null) {
                text.append(msg);
            }
            consoleScroller.scroll();
            setLineCount();
        }
    };

    private final Runnable textLoad = () -> {
        String nextMsg = (String) rawQueue.poll();
        if (nextMsg == null)
            return;

        nextMsg = nextMsg.trim() + '\n';
        PrecomputedTextCompat.create(nextMsg, textParams);
        outQueue.add(nextMsg);

        long curr = SystemClock.uptimeMillis();
        if (lastTime + UPDATE_TIME_MS < curr) {
            lastTime = curr;
            uiHandle.post(textUpdate);
        }
    };

    private void clearText() {
        text.setText(null);
    }

    private void setErrorCount() {
        consoleError.setText(String.format(Locale.US, errorFormat, errorCounter));
    }

    private void setLineCount() {
        consoleLines.setText(String.format(Locale.US, linesFormat, text.getLineCount()));
    }

    private static void initHandle() {
        if (textHandle == null) {
            consoleThread.start();
            textHandle = new Handler(consoleThread.getLooper());
        }
    }

    public ConsoleWidget(@NonNull Context context) {
        this(context, null);
    }

    public ConsoleWidget(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public enum Status {
        Disconnected(R.color.red),
        Connected(R.color.green),
        Testing(R.color.blue);

        @ColorRes
        final
        int color;

        Status(@ColorRes int color) {
            this.color = color;
        }
    }

    public void newError() {
        errorCounter++;
        uiHandle.post(this::setErrorCount);
    }

    private void updateStatus() {
        consoleStatus.setText(String.format(Locale.US, statusFormat, currentStatus.toString()));
        consoleStatus.setBackgroundColor(getResources().getColor(currentStatus.color, getContext().getTheme()));
    }

    public void setStatus(Status status) {
        currentStatus = status;
        uiHandle.post(this::updateStatus);
    }

    public ConsoleWidget(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View.inflate(context, R.layout.widget_console, this);

        scrollToEndImage = findViewById(R.id.scrollToEndImage);
        consoleScroller = findViewById(R.id.consoleScroller);
        text = findViewById(R.id.consoleText);
        consoleLines = findViewById(R.id.consoleLines);
        consoleError = findViewById(R.id.consoleError);
        consoleStatus = findViewById(R.id.consoleStatus);
        consoleMode = findViewById(R.id.consoleMode);

        textParams = new PrecomputedTextCompat.Params.Builder(text.getPaint()).build();
        consoleScroller.setScrollerStatusListener(enabled -> scrollToEndImage.setAlpha(enabled ? 1 : 0.5f));
        scrollToEndImage.setOnClickListener(v -> consoleScroller.toggle());

        linesFormat = context.getString(R.string.console_line_format);
        errorFormat = context.getString(R.string.console_error_format);
        statusFormat = context.getString(R.string.console_status_format);
        modeFormat = context.getString(R.string.console_mode_format);

        setLineCount();
        setErrorCount();
        updateStatus();
        setMode("Nil");

        initHandle();
        UITester.addTest(this);
    }

    public void setMode(String mode) {
        consoleMode.setText(String.format(Locale.US, modeFormat, mode));
    }

    public void clear() {
        uiHandle.post(this::clearText);
        uiHandle.post(this::setLineCount);
        errorCounter = -1;
        newError();
    }

    public void enable(boolean enabled) {
        if (run == enabled)
            return;
        if (enabled) {
            run = true;
        } else {
            run = false;
            rawQueue.clear();
            outQueue.clear();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        UITester.removeTest(this);
        enable(false);
        consoleThread.quitSafely();
        super.finalize();
    }

    @Override
    public void testUI(float percent) {
        if (percent > 0.5)
            post(UITester.rndStr((int) (percent * 50)));
        else
            systemPost(UITester.rndStr((int) (percent * 10)), UITester.rndStr((int) (percent * 50)));

        if (percent == 0) {
            uiHandle.postDelayed(this::clear, 100);
            setStatus(currentStatus);
        } else {
            newError();
            consoleStatus.setText(String.format(Locale.US, statusFormat, Status.Testing.toString()));
            consoleStatus.setBackgroundColor(getResources().getColor(Status.Testing.color, getContext().getTheme()));
        }
    }

    public void post(@NonNull CharSequence msg) {
        if (run) {
            rawQueue.add(msg);
            textHandle.post(textLoad);
        }
    }

    private static final String systemPostFormat = "[SYSTEM] [%s] %s";

    public void systemPost(@NonNull String tag, @NonNull String msg) {
        rawQueue.add(String.format(Locale.US, systemPostFormat, tag, msg));
        textHandle.post(textLoad);
    }

}
