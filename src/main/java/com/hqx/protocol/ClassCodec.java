package com.hqx.protocol;

import com.google.gson.*;

import java.lang.reflect.Type;

/**
 * @Description 因为gson工具包的默认序列化器在序列化String.class类似的的类型数据时出现异常，所以为class类型的数据专门自定义序列化、反序列化器
 * @Create by hqx
 * @Date 2023/12/4 22:09
 */
public class ClassCodec implements JsonSerializer<Class<?>>, JsonDeserializer<Class<?>> {

    // 用法
    public static void main(String[] args) {
        Gson gson = new GsonBuilder().registerTypeAdapter(Class.class, new ClassCodec()).create();
        System.out.println(gson.toJson(String.class));
    }

    @Override
    public Class<?> deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        String str = jsonElement.getAsString();
        try {
            return Class.forName(str);
        } catch (ClassNotFoundException e) {
           throw new JsonParseException(e);
        }
    }

    @Override
    public JsonElement serialize(Class<?> aClass, Type type, JsonSerializationContext jsonSerializationContext) {
        // class -> json
        return new JsonPrimitive(aClass.getName());
    }
}
