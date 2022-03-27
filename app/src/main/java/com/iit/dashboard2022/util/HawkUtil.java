package com.iit.dashboard2022.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import androidx.appcompat.app.AppCompatActivity;
import com.iit.dashboard2022.MainActivity;

import javax.net.ssl.HttpsURLConnection;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.Locale;

/**
 * Utility class with general helpers for the dashboard
 *
 * @author Noah Husby
 */
public class HawkUtil {

    /**
     * Sets the window's flags according to pre-set parameters
     *
     * @param window {@link Window}
     */
    public static void setWindowFlags(Window window) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.getInsetsController().hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
        } else {
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                                                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                                                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                                                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    }

    /**
     * Checks if the host device has a network connection
     *
     * @param context {@link Context}
     * @return True if host has connection, false if not
     */
    public static boolean checkInternetConnection(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkCapabilities capabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());
            return capabilities != null && (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN));
        }
        return false;
    }

    /**
     * Gets the application file directory
     *
     * @return The file directory if global context is loaded, null otherwise
     */
    public static File getFilesDir() {
        AppCompatActivity context = MainActivity.GLOBAL_CONTEXT;
        return context == null ? null : context.getFilesDir();
    }

    public static File getLogFilesDir() {
        return new File(getFilesDir(), "logs");
    }

    /**
     * Creates a new {@link HttpsURLConnection} instance
     *
     * @param url         URL of request
     * @param method      Method of request (GET, POST, PUT, DELETE)
     * @param accept      Accept
     * @param contentType Content-Type
     * @param authToken   X-Auth-Token
     * @return {@link HttpsURLConnection}
     * @throws IOException if a connection cannot be formed
     */
    public static HttpsURLConnection createHttpConnection(String url, String method, String accept, String contentType, String authToken) throws IOException {
        return createHttpConnection(new URL(url), method, accept, contentType, authToken);
    }

    /**
     * Creates a new {@link HttpsURLConnection} instance
     *
     * @param url         URL of request
     * @param method      Method of request (GET, POST, PUT, DELETE)
     * @param accept      Accept
     * @param contentType Content-Type
     * @param authToken   X-Auth-Token
     * @return {@link HttpsURLConnection}
     * @throws IOException if a connection cannot be formed
     */
    public static HttpsURLConnection createHttpConnection(URL url, String method, String accept, String contentType, String authToken) throws IOException {
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod(method);
        if (accept != null) {
            conn.setRequestProperty("Accept", accept);
        }
        if (contentType != null) {
            conn.setRequestProperty("Content-Type", contentType);
        }
        conn.setRequestProperty("X-Auth-Token", authToken);
        return conn;
    }

    /**
     * Converts amount of bytes to human-readable format
     *
     * @param bytes Size of file
     * @return Size of file formatted as a String
     */
    public static String humanReadableBytes(long bytes) {
        long absB = bytes == Long.MIN_VALUE ? Long.MAX_VALUE : Math.abs(bytes);
        if (absB < 1024) {
            return bytes + " B";
        }
        long value = absB;
        CharacterIterator ci = new StringCharacterIterator("KMGTPE");
        for (int i = 40; i >= 0 && absB > 0xfffccccccccccccL >> i; i -= 10) {
            value >>= 10;
            ci.next();
        }
        value *= Long.signum(bytes);
        return String.format(Locale.ENGLISH, "%.1f %ciB", value / 1024.0, ci.current());
    }

    /**
     * Adds a upload file section to the request
     *
     * @param fieldName  name attribute in <input type="file" name="..." />
     * @param uploadFile a File to be uploaded
     * @throws IOException
     */
    public static void addFilePart(PrintWriter writer, OutputStream outputStream, String boundary, String fieldName, File uploadFile)
            throws IOException {
        String fileName = uploadFile.getName();
        writer.append("--" + boundary).append(Constants.LINE_FEED);
        writer.append(
                "Content-Disposition: form-data; name=\"" + fieldName
                + "\"; filename=\"" + fileName + "\"")
                .append(Constants.LINE_FEED);
        writer.append(
                "Content-Type: "
                + URLConnection.guessContentTypeFromName(fileName))
                .append(Constants.LINE_FEED);
        writer.append("Content-Transfer-Encoding: binary").append(Constants.LINE_FEED);
        writer.append(Constants.LINE_FEED);
        writer.flush();

        FileInputStream inputStream = new FileInputStream(uploadFile);
        byte[] buffer = new byte[4096];
        int bytesRead = -1;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        outputStream.flush();
        inputStream.close();

        writer.append(Constants.LINE_FEED);
        writer.flush();
    }

}
