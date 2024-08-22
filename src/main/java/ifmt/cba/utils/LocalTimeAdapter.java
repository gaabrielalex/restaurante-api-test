package ifmt.cba.utils;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class LocalTimeAdapter extends TypeAdapter<LocalTime> {
    private static final DateTimeFormatter formatterWithMillis = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
    private static final DateTimeFormatter formatterWithoutMillis = DateTimeFormatter.ofPattern("HH:mm:ss");

    @Override
    public void write(JsonWriter out, LocalTime value) throws IOException {
        if (value != null) {
            out.value(value.format(formatterWithMillis)); // Mantém a formatação com milissegundos
        } else {
            out.nullValue();
        }
    }

    @Override
    public LocalTime read(JsonReader in) throws IOException {
        String time = in.nextString();
        try {
            // Tenta parsear com milissegundos
            return LocalTime.parse(time, formatterWithMillis);
        } catch (DateTimeParseException e1) {
            try {
                // Tenta parsear sem milissegundos
                LocalTime parsedTime = LocalTime.parse(time, formatterWithoutMillis);
                // Adiciona três casas decimais ".000" se não houver milissegundos
                return LocalTime.parse(parsedTime.format(formatterWithMillis), formatterWithMillis);
            } catch (DateTimeParseException e2) {
                throw new IOException("Failed to parse LocalTime: " + time, e2);
            }
        }
    }
}
