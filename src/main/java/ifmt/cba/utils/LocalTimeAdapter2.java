package ifmt.cba.utils;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class LocalTimeAdapter2 extends TypeAdapter<LocalTime> {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    @Override
    public void write(JsonWriter jsonWriter, LocalTime localTime) throws IOException {
        jsonWriter.value(localTime.format(formatter));
    }

    @Override
    public LocalTime read(JsonReader jsonReader) throws IOException {
        String time = jsonReader.nextString();
        // Tente analisar com diferentes formatos
        try {
            return LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm:ss.SSS"));
        } catch (DateTimeParseException e) {
            return LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm:ss.SS"));
        }
    }

}
