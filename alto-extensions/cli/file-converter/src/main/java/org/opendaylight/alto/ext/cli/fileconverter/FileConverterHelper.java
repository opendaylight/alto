package org.opendaylight.alto.ext.cli.fileconverter;

import java.io.File;
import java.nio.charset.StandardCharsets;

import com.google.common.io.Files;

class FileConverterHelper {

    public String load(String path) throws Exception {
        File file = new File(path);
        return Files.toString(file, StandardCharsets.US_ASCII);
    }

    public void save(String path, String content) throws Exception {
        File file = new File(path);
        Files.write(content, file, StandardCharsets.US_ASCII);
    }
}
