package org.togglz.core.util;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * This converter is able to convert string maps to simple strings and vice versa.
 * 
 * @author Christian Kaltepoth
 */
public class MapConverter {

    private boolean newLines = true;

    /**
     * Use {@link #create()}
     */
    private MapConverter() {
    }

    /**
     * Creates a new instance of the {@link MapConverter}.
     */
    public static MapConverter create() {
        return new MapConverter();
    }

    /**
     * Don't use new line characters in the string representation.
     */
    public MapConverter withoutNewLines() {
        this.newLines = false;
        return this;
    }

    /**
     * Allow new lines in the string representation
     */
    public MapConverter withNewLines() {
        this.newLines = true;
        return this;
    }

    /**
     * Converts the given map to a string representation that can be used to restore the map using
     * {@link #convertFromString(String)}.
     */
    public String convertToString(Map<String, String> map) {

        try {

            // the format is based on the properties output format
            Properties props = new Properties();
            props.putAll(map);
            Writer writer = new StringWriter();
            props.store(writer, null);

            // we need a list of strings so we can further process the format
            List<String> lines = new ArrayList<String>();
            for (String line : writer.toString().split("\r?\n")) {
                // comments and empty lines are skipped
                if (!line.trim().isEmpty() && !line.trim().startsWith("#")) {
                    lines.add(line.trim());
                }
            }

            // sort by key so create a canonical format
            Collections.sort(lines);

            // build the output string
            StringBuilder builder = new StringBuilder();
            for (String line : lines) {

                if (newLines) {
                    builder.append(line);
                    builder.append("\r\n");
                }
                else {
                    if (builder.length() > 0) {
                        builder.append("&");
                    }
                    builder.append(line.replace("&", "\\u0026"));
                }

            }
            return builder.toString();

        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

    }

    /**
     * Restores a map that has been converted into a string using {@link #convertToString(Map)}
     */
    public Map<String, String> convertFromString(String s) {

        try {

            String input = newLines ? s : s.replace('&', '\n');

            Properties props = new Properties();
            if (s != null) {
                props.load(new StringReader(input));
            }

            LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();
            for (Entry<Object, Object> entry : props.entrySet()) {
                result.put(entry.getKey().toString(), entry.getValue().toString());
            }
            return result;

        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

    }

}