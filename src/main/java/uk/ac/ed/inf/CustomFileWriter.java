package uk.ac.ed.inf;

import java.io.*;
import java.nio.charset.StandardCharsets;

public abstract class CustomFileWriter {
    private final File file;

    public CustomFileWriter(String filepath) {
        this.file = new File(filepath);
    }

    protected void write(String value) throws IOException {
        // TODO: make sure that if the file exists, overwrite
        if (!this.file.exists()) {
            CustomLogger.getLogger().log("File does not exist.");
            this.file.getParentFile().mkdirs();
            this.file.createNewFile();
        }
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(this.file.getPath()), StandardCharsets.UTF_8))) {
            writer.write(value);
        }
    }
}
