package com.iit.dashboard2022.ui.widget.console;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.text.PrecomputedTextCompat;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.encoder.LayoutWrappingEncoder;
import com.iit.dashboard2022.R;
import com.iit.dashboard2022.logging.StringLogger;
import com.iit.dashboard2022.ui.UITester;
import com.iit.dashboard2022.ui.anim.AnimSetting;
import com.iit.dashboard2022.ui.anim.TranslationAnim;
import com.iit.dashboard2022.ui.widget.WidgetUpdater;

import java.util.Locale;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;


public class ConsoleWidget extends ConstraintLayout implements WidgetUpdater.Widget, UITester.TestUI, StringLogger {

    private static final LinkedBlockingQueue<CharSequence> rawQueue = new LinkedBlockingQueue<>();
    private static final ConcurrentLinkedQueue<CharSequence> outQueue = new ConcurrentLinkedQueue<>();
    private static final HandlerThread consoleThread = new HandlerThread("Console Thread");
    private static final Handler uiHandle = new Handler(Looper.getMainLooper());
    private static final String systemPostFormat = "[SYSTEM] [%s] ";
    private final TextView text, consoleLines, consoleError, consoleStatus, consoleMode;
    private final String linesFormat, errorFormat, statusFormat, modeFormat;
    private final ImageView scrollToEndImage, scrollToStartImage;
    private final ConsoleScroller consoleScroller;
    private final TranslationAnim consoleAnim;
    private final TextLoader textLoader;

    private Status currentStatus = Status.Disconnected;
    private int errorCounter = 0;
    private boolean run = false;
    private boolean testingState = false;

    public ConsoleWidget(@NonNull Context context) {
        this(context, null);
    }

    public ConsoleWidget(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ConsoleWidget(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View.inflate(context, R.layout.widget_console, this);

        consoleAnim = new TranslationAnim(this, TranslationAnim.X_AXIS, TranslationAnim.ANIM_FORWARD);
        consoleAnim.startWhenReady();

        scrollToEndImage = findViewById(R.id.scrollToEndImage);
        scrollToStartImage = findViewById(R.id.scrollToStartImage);
        consoleScroller = findViewById(R.id.consoleScroller);
        text = findViewById(R.id.consoleText);
        consoleLines = findViewById(R.id.consoleLines);
        consoleError = findViewById(R.id.consoleError);
        consoleStatus = findViewById(R.id.consoleStatus);
        consoleMode = findViewById(R.id.consoleMode);

        consoleScroller.setScrollerStatusListener(enabled -> scrollToEndImage.setAlpha(enabled ? 1 : 0.5f));
        scrollToEndImage.setOnClickListener(v -> {
            consoleScroller.toggle();
            consoleScroller.scrollDown();
        });
        scrollToStartImage.setOnClickListener(v -> {
            consoleScroller.scrollUp();
            scrollToStartImage.setAlpha(1f);
            postDelayed(() -> scrollToStartImage.setAlpha(0.5f), AnimSetting.ANIM_DURATION / 2);
        });
        scrollToStartImage.setAlpha(0.5f);

        linesFormat = context.getString(R.string.console_line_format);
        errorFormat = context.getString(R.string.console_error_format);
        statusFormat = context.getString(R.string.console_status_format);
        modeFormat = context.getString(R.string.console_mode_format);

        setLineCount();
        setErrorCount();
        updateStatus();
        setMode("Nil");

        textLoader = new TextLoader(text.getPaint(), text);
        textLoader.start();

        UITester.addTest(this);
        WidgetUpdater.add(this);
    }

    public static CharSequence trimCharSequence(@NonNull CharSequence sequence) {
        int len = sequence.length();
        int st = 0;

        while ((st < len) && (sequence.charAt(st) <= ' ')) {
            st++;
        }
        while ((st < len) && (sequence.charAt(len - 1) <= ' ')) {
            len--;
        }
        return ((st > 0) || (len < sequence.length())) ? sequence.subSequence(st, len) : sequence;
    }

    @UiThread
    private void clearText() {
        text.setText(null);
    }

    @UiThread
    private void setErrorCount() {
        consoleError.setText(String.format(Locale.US, errorFormat, errorCounter));
    }

    @UiThread
    private void setLineCount() {
        consoleLines.setText(String.format(Locale.US, linesFormat, text.getLineCount()));
    }

    @UiThread
    private void updateStatus() {
        consoleStatus.setText(String.format(Locale.US, statusFormat, currentStatus.toString()));
        consoleStatus.setBackgroundColor(getResources().getColor(currentStatus.color, getContext().getTheme()));
    }

    public void newError() {
        errorCounter++;
        WidgetUpdater.post();
    }

    public void setStatus(Status status) {
        currentStatus = status;
        WidgetUpdater.post();
    }

    public TranslationAnim getAnimator() {
        return consoleAnim;
    }

    public void setMode(String mode) {
        consoleMode.setText(String.format(Locale.US, modeFormat, mode));
    }

    @UiThread
    public void clear() {
        clearText();
        setLineCount();
        errorCounter = -1;
        newError();
    }

    public void enable(boolean enabled) {
        if (run == enabled) {
            return;
        }
        if (enabled) {
            run = true;
        } else {
            run = false;
            rawQueue.clear();
            outQueue.clear();
        }
    }

    public void post(@NonNull CharSequence msg) {
        if (run) {
            rawQueue.add(msg);
        }
    }

    public void systemPost(@NonNull String tag, @NonNull CharSequence msg) {
        textLoader.addLimit();
        rawQueue.add(TextUtils.concat(String.format(Locale.US, systemPostFormat, tag), msg));
    }

    @Override
    protected void finalize() throws Throwable {
        WidgetUpdater.remove(this);
        UITester.removeTest(this);
        enable(false);
        consoleThread.quitSafely();
        super.finalize();
    }

    @Override
    public void onWidgetUpdate() {
        uiHandle.post(() -> {
            CharSequence msg;
            while ((msg = outQueue.poll()) != null) {
                text.append(msg);
            }
            consoleScroller.scrollDown();
            setErrorCount();
            updateStatus();
            setLineCount();
        });
    }

    @Override
    public void testUI(float percent) {
        if (percent > 0.1) {
            post(UITester.rndStr((int) (percent * 50)));
        } else {
            systemPost(UITester.rndStr((int) (percent * 10)), UITester.rndStr((int) (percent * 50)));
        }

        if (percent == 0) {
            uiHandle.postDelayed(this::clear, 100);
            setStatus(currentStatus);
            testingState = false;
        } else {
            newError();
            if (!testingState) {
                uiHandle.post(() -> {
                    consoleStatus.setText(String.format(Locale.US, statusFormat, Status.Testing));
                    consoleStatus.setBackgroundColor(getResources().getColor(Status.Testing.color, getContext().getTheme()));
                });
                testingState = true;
            }
        }
    }

    @Override
    public void onLoggingEvent(ILoggingEvent event, LayoutWrappingEncoder<ILoggingEvent> encoder) {
        post(encoder.getLayout().doLayout(event));
    }

    public enum Status {
        Disconnected(R.color.red),
        Connected(R.color.green),
        Attached(R.color.blue),
        Testing(R.color.midground);

        @ColorRes
        final
        int color;

        Status(@ColorRes int color) {
            this.color = color;
        }
    }

    private static class TextLoader extends Thread {
        private final LinkedBlockingQueue<CharSequence> rawQueue;
        private final PrecomputedTextCompat.Params textParams;
        private final TextView text;
        private int limit = 0;

        TextLoader(TextPaint paint, TextView text) {
            this.rawQueue = ConsoleWidget.rawQueue;
            this.text = text;

            textParams = new PrecomputedTextCompat.Params.Builder(paint).build();
        }

        @Override
        public void run() {
            while (true) {
                try {
                    CharSequence nextMsg;
                    nextMsg = rawQueue.take();

                    if (nextMsg != null) {
                        nextMsg = TextUtils.concat(trimCharSequence(nextMsg), "\n");
                        PrecomputedTextCompat.create(nextMsg, textParams);
                        outQueue.add(nextMsg);
                    }

                    if (limit == 0 && text.getLineCount() > 5000) {
                        uiHandle.post(() -> text.setText(null));
                    } else if (limit > 0) {
                        limit--;
                    }

                    WidgetUpdater.post();
                } catch (InterruptedException e) {
                    return;
                }
            }
        }

        public void addLimit() {
            limit++;
        }
    }
}
