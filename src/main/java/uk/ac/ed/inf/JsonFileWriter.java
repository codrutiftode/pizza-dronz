package uk.ac.ed.inf;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.io.File;
import java.io.IOException;

public class JsonFileWriter {
    private final ObjectMapper mapper;
    private final ObjectWriter writer;
    private final File file;

    public JsonFileWriter(String filepath) {
        this.mapper = new ObjectMapper();
        this.writer = mapper.writer(new DefaultPrettyPrinter());
        this.file = new File(filepath);
    }

    public void write(Object value) throws IOException {
        if (!this.file.exists()) {
            CustomLogger.getLogger().log("File does not exist.");
            this.file.getParentFile().mkdirs();
            this.file.createNewFile();
        }
        this.writer.writeValue(this.file, value);
    }
}
