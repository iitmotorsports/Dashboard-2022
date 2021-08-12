package com.iit.dashboard2022.ui.widget.console;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.text.PrecomputedTextCompat;

import com.iit.dashboard2022.R;

import java.util.concurrent.ConcurrentLinkedQueue;


public class ConsoleWidget extends ConstraintLayout {

    private static final ConcurrentLinkedQueue<CharSequence> rawQueue = new ConcurrentLinkedQueue<>();
    private static final ConcurrentLinkedQueue<CharSequence> outQueue = new ConcurrentLinkedQueue<>();
    private static final HandlerThread consoleThread = new HandlerThread("Console Thread");
    private static final Handler textPush = new Handler();
    private static Handler textHandle;

    private PrecomputedTextCompat.Params textParams;
    private boolean run = false;

    private final ConsoleScroller consoleScroller;
    private final ImageView scrollToEndImage;
    private TextView text, consoleLines, consoleError;

    private final Runnable textLoad = () -> {
        while (run) {
            String nextMsg = (String) rawQueue.poll();
            if (nextMsg == null)
                continue;
            nextMsg = nextMsg.trim() + '\n';
            PrecomputedTextCompat.create(nextMsg, textParams);
            outQueue.add(nextMsg);
        }
    };

    private final Runnable textUpdate = new Runnable() {
        @Override
        public void run() {
            CharSequence msg;
            while ((msg = outQueue.poll()) != null) {
                text.append(msg);
                consoleScroller.scroll();
            }
            if (run)
                textPush.postDelayed(this, 100);
        }
    };

    private final Runnable clearText = () -> text.setText("");

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

    public ConsoleWidget(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View.inflate(context, R.layout.widget_console, this);

        scrollToEndImage = findViewById(R.id.scrollToEndImage);
        consoleScroller = findViewById(R.id.consoleScroller);
        text = findViewById(R.id.consoleText);
        consoleLines = findViewById(R.id.consoleLines);

        textParams = new PrecomputedTextCompat.Params.Builder(text.getPaint()).build();
        consoleScroller.setScrollerStatusListener(enabled -> scrollToEndImage.setAlpha(enabled ? 1 : 0.5f));
        scrollToEndImage.setOnClickListener(v -> consoleScroller.toggle());

        initHandle();
    }

    public void clear() {
        textPush.post(clearText);
    }

    public void enable(boolean enabled) {
        if (run == enabled)
            return;
        if (enabled) {
            run = true;
            textPush.post(textUpdate);
            textHandle.post(textLoad);
        } else {
            run = false;
        }
    }

    @Override
    protected void finalize() throws Throwable {
        enable(false);
        consoleThread.quitSafely();
        super.finalize();
    }

    public void post(@NonNull CharSequence msg) {
        rawQueue.add(msg);
    }

}
