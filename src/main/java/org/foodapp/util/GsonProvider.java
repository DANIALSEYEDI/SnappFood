package org.foodapp.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
 import org.foodapp.util.LocalDateTimeAdapter;

 import java.time.LocalDateTime;

public class GsonProvider {
    public static final Gson INSTANCE = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();
}
