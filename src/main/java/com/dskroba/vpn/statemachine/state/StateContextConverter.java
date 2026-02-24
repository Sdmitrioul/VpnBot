package com.dskroba.vpn.statemachine.state;

import com.dskroba.vpn.statemachine.effect.Effect;
import com.dskroba.vpn.statemachine.effect.EffectTypeAdapter;
import com.dskroba.vpn.statemachine.state.descriptors.CommandStatesDescriptors;
import com.dskroba.vpn.statemachine.state.descriptors.UsersDescriptors;
import com.dskroba.vpn.statemachine.state.descriptors.VpnDescriptors;
import com.dskroba.vpn.statemachine.state.descriptors.StateDescriptor;
import com.dskroba.vpn.storage.ValueConverter;
import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

public class StateContextConverter implements ValueConverter<StateContext> {
    private static final StateDescriptorTypeAdapter STATE_DESCRIPTOR_TYPE_ADAPTER = new StateDescriptorTypeAdapter();
    private static final EffectTypeAdapter EFFECT_TYPE_ADAPTER = new EffectTypeAdapter();
    private static final Gson RAW_GSON = new Gson();
    private static final Gson GSON = new GsonBuilder()
            .registerTypeHierarchyAdapter(Effect.class, EFFECT_TYPE_ADAPTER)
            .registerTypeHierarchyAdapter(StateDescriptor.class, STATE_DESCRIPTOR_TYPE_ADAPTER)
            .registerTypeAdapter(Instant.class, new InstantTypeAdapter())
            .create();

    @Override
    public StateContext parse(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        return GSON.fromJson(value, StateContext.class);
    }

    @Override
    public String convertToString(StateContext value) {
        if (value == null) {
            return "";
        }
        return GSON.toJson(value);
    }

    private static class StateDescriptorTypeAdapter implements JsonSerializer<StateDescriptor>, JsonDeserializer<StateDescriptor> {
        private static final String TYPE_FIELD = "type";
        private static final String DATA_FIELD = "data";

        private static final Map<String, Class<? extends StateDescriptor>> TYPE_MAP = Map.of(
                "command", CommandStatesDescriptors.class,
                "vpn", VpnDescriptors.class,
                "users", UsersDescriptors.class
                //TODO add more
        );

        private static final Map<Class<? extends StateDescriptor>, String> REVERSE_MAP =
                TYPE_MAP.entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

        @Override
        public JsonElement serialize(StateDescriptor src, Type typeOfSrc, JsonSerializationContext context) {
            var obj = new JsonObject();
            obj.addProperty(TYPE_FIELD, REVERSE_MAP.get(src.getClass()));
            obj.add(DATA_FIELD, new JsonPrimitive(src.name()));
            return obj;
        }

        @Override
        public StateDescriptor deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            var obj = json.getAsJsonObject();
            String type = obj.remove(TYPE_FIELD).getAsString();
            Class<? extends StateDescriptor> clazz = TYPE_MAP.get(type);
            if (clazz == null) {
                throw new JsonParseException("Unknown StateDescriptor type: " + type);
            }
            return RAW_GSON.fromJson(obj.get(DATA_FIELD).getAsString(), clazz);
        }
    }

    private static class InstantTypeAdapter extends TypeAdapter<Instant> {
        @Override
        public void write(JsonWriter out, Instant value) throws IOException {
            if (value == null) {
                out.nullValue();
            } else {
                out.value(value.toString()); // ISO-8601: "2025-01-15T10:30:00Z"
            }
        }

        @Override
        public Instant read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return null;
            }
            return Instant.parse(in.nextString());
        }
    }
}
