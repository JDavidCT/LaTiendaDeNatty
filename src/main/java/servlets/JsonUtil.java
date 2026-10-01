package servlets;

import java.io.IOException;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

final class JsonUtil {
    static final Gson GSON = new Gson();

    private JsonUtil() {}

    static JsonObject read(HttpServletRequest request) throws IOException {
        try {
            var body = JsonParser.parseReader(request.getReader());
            if (!body.isJsonObject()) throw new IllegalArgumentException("El cuerpo debe ser un objeto JSON.");
            return body.getAsJsonObject();
        } catch (JsonParseException | IllegalStateException e) {
            throw new IllegalArgumentException("El cuerpo JSON no es válido.", e);
        }
    }

    static void send(HttpServletResponse response, int status, Object data) throws IOException {
        response.setStatus(status);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Cache-Control", "no-store");
        response.getWriter().write(GSON.toJson(data));
    }

    static void error(HttpServletResponse response, int status, String message) throws IOException {
        send(response, status, Map.of("mensaje", message));
    }

    static String string(JsonObject body, String field) {
        if (!body.has(field) || body.get(field).isJsonNull()) return null;
        if (!body.get(field).isJsonPrimitive() || !body.getAsJsonPrimitive(field).isString()) {
            throw new IllegalArgumentException("El campo " + field + " debe ser texto.");
        }
        return body.get(field).getAsString();
    }
}