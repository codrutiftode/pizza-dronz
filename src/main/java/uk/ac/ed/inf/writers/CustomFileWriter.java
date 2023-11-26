package uk.ac.ed.inf.writers;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Writes data to a given file, creating necessary directories / files
 */
public abstract class CustomFileWriter {
    private final File file;

    public CustomFileWriter(String filepath) {
        this.file = new File(filepath);
    }

    protected void write(String value) throws IOException {
        if (!this.file.exists()) {
            if (!this.file.getParentFile().mkdirs()) {
                throw new IOException("Could not create output file parent directories for: " + this.file.getPath());
            }
            if (!this.file.createNewFile()) {
                throw new IOException("Could not create output file: " + this.file.getPath());
            }
        }
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(this.file.getPath()), StandardCharsets.UTF_8))) {
            writer.write(value);
        }
    }
}
