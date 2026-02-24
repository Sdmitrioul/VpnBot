package com.dskroba.vpn.statemachine.effect;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.Map;
import java.util.stream.Collectors;

public class EffectTypeAdapter implements JsonSerializer<Effect>, JsonDeserializer<Effect> {
    private static final Gson RAW_GSON = new Gson();
    private static final Gson GSON = new GsonBuilder()
            .registerTypeHierarchyAdapter(Effect.class, new EffectTypeAdapter())
            .create();

    private static final Map<String, Class<? extends Effect>> TYPE_MAP = Map.of(
            "file", FileEffect.class,
            "message", MessageEffect.class,
            "select", SelectEffect.class,
            "composite", CompositeEffect.class
    );

    private static final Map<Class<? extends Effect>, String> CLASS_MAP =
            TYPE_MAP.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

    @Override
    public JsonElement serialize(Effect src, Type type, JsonSerializationContext ctx) {
        JsonObject obj;
        if (src instanceof CompositeEffect ce) {
            obj = new JsonObject();
            JsonArray array = new JsonArray();
            for (Effect effect : ce.payload()) {
                array.add(GSON.toJsonTree(effect));
            }
            obj.add("payload", array);
        } else {
            obj = RAW_GSON.toJsonTree(src).getAsJsonObject();
        }
        obj.addProperty("type", CLASS_MAP.get(src.getClass()));
        return obj;
    }

    @Override
    public Effect deserialize(JsonElement json, Type type, JsonDeserializationContext ctx) throws JsonParseException {
        JsonObject obj = json.getAsJsonObject();
        String kind = obj.remove("type").getAsString();
        Class<? extends Effect> clazz = TYPE_MAP.get(kind);
        if (clazz == null) {
            throw new JsonParseException("Unknown effect type: " + kind);
        }
        if (clazz == CompositeEffect.class) {
            LinkedList<Effect> list = new LinkedList<>();
            for (var element : obj.getAsJsonArray("payload")) {
                list.add(GSON.fromJson(element, Effect.class));
            }
            return new CompositeEffect(list);
        }
        return RAW_GSON.fromJson(obj, clazz);
    }
}
