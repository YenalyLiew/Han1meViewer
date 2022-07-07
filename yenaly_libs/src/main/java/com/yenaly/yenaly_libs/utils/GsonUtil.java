package com.yenaly.yenaly_libs.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import org.jetbrains.annotations.Contract;

import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

/**
 * Gson工具库
 *
 * @ProjectName : YenalyModule
 * @Author : Yenaly Liew
 * @Time : 2022/04/17 017 23:05
 * @Description : Description...
 */
@SuppressWarnings("unused")
public class GsonUtil {

    public static final Gson gson;

    static {
        gson = new GsonBuilder()
                .excludeFieldsWithModifiers(Modifier.TRANSIENT, Modifier.STATIC)
                .create();
    }

    private GsonUtil() {
    }

    /**
     * 转换为Json字符串
     *
     * @param obj 对象
     * @return Json字符串
     */
    @Nullable
    public static String toJson(@Nullable Object obj) {
        return gson.toJson(obj);
    }

    /**
     * 解析Json数组
     *
     * @param json  Json字符串
     * @param model 模型
     *              (Java: Object[].class)
     *              (Kotlin: Array&lt;Any&gt;::class.java)
     * @param <T>   类型
     * @return 模型列表
     */
    @NonNull
    public static <T> List<T> parseGsonArray(String json, Class<T[]> model) {
        return Arrays.asList(gson.fromJson(json, model));
    }

    /**
     * 解析Json字符串
     *
     * @param json Json字符串
     * @param type 类型
     * @param <T>  类型
     * @return 对象
     */
    public static <T> T fromJson(@Nullable String json, Type type) {
        return gson.fromJson(json, type);
    }

    /**
     * @param json  Json字符串
     * @param clazz class
     * @param <T>   类型
     * @return 对象
     */
    public static <T> T fromJson(@Nullable String json, Class<T> clazz) {
        return gson.fromJson(json, clazz);
    }

    /**
     * 解析Json字符串
     *
     * @param json                 Json字符串
     * @param rawType              原始类型 List.class
     * @param genericArgumentTypes 泛型参数类型数组 new Class[]{mClazz}
     * @param <T>                  原始类型
     * @return 对象
     */
    public static <T> T fromJson(
            @Nullable String json,
            @NonNull final Type rawType,
            @NonNull final Type[] genericArgumentTypes
    ) throws JsonSyntaxException {
        Type resultType = new ParameterizedType() {
            @NonNull
            @Override
            public Type[] getActualTypeArguments() {
                return genericArgumentTypes;
            }

            @NonNull
            @Override
            public Type getRawType() {
                return rawType;
            }

            @Nullable
            @Contract(pure = true)
            @Override
            public Type getOwnerType() {
                return null;
            }
        };
        return gson.fromJson(json, resultType);
    }
}
