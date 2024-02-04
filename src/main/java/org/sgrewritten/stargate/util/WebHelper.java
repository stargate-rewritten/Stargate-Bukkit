package org.sgrewritten.stargate.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

public class WebHelper {

    private WebHelper(){
        throw new IllegalStateException("Utility class");
    }

    public static void downloadFile(String link, File file) throws IOException {
        URL url = new URL(link);
        try (InputStream inputStream = url.openStream()) {
            try (OutputStream outputStream = new FileOutputStream(file)) {
                inputStream.transferTo(outputStream);
            }
        }
    }

}
