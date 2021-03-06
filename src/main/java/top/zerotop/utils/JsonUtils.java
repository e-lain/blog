package top.zerotop.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class JsonUtils {
    private static final Logger logger = LoggerFactory.getLogger(JsonUtils.class);

    private static ObjectMapper objectMapper = new ObjectMapper();

    private static ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.findAndRegisterModules();
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        return objectMapper;
    }

    public static String toJson(Object o) {
        try {
            return objectMapper().writeValueAsString(o).toString();
        } catch (IOException e) {
            return "";
        }
    }

    public static <T> T toObject(String json, Class<T> clazz) {
        try {
            return objectMapper().readValue(json, clazz);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            logger.info("convert to object failed...");
            return null;
        }
    }

    /**
     * 传入json对象中clazz的属性名
     *
     * @return
     */
    public static <T> T toSubObject(String json, String attrName, Class<T> clazz) {
        if (!StringUtils.hasText(json)) {
            return null;
        }
        try {
            JsonNode jsonNode = objectMapper().readTree(json).get(attrName);
            JavaType javaType = createType(clazz);
            T t = objectMapper().readValue(jsonNode.toString(), javaType);
            return t;
        } catch (IOException e) {
            logger.info("wrong sub type ...");
            e.printStackTrace();
            return null;
        }
    }

    public static <T> List<T> fromJson(String json, Class<T> clazz) {
        List<T> result = new ArrayList<>();
        if (!StringUtils.hasText(json)) {
            return result;
        }
        try {
            JsonNode jsonNode = objectMapper().readTree(json);
            JavaType javaType = createJavaType(ArrayList.class, clazz);
            result = objectMapper().readValue(jsonNode.toString(), javaType);
        } catch (Exception e) {
            logger.warn(" convert to list wrong ...");
            e.printStackTrace();
        }
        return CollectionUtils.isEmpty(result) ? new ArrayList<>() : result;
    }

    public static JavaType createType(Class clazz) {
        return objectMapper().getTypeFactory().constructType(clazz);
    }

    public static JavaType createJavaType(Class<?> collectionClass, Class... elementClasses) {
        return objectMapper().getTypeFactory().constructParametricType(collectionClass, elementClasses);
    }

    public static String toJsonString(HttpServletRequest req) {
        StringBuilder json = new StringBuilder();
        try {
            InputStream in = req.getInputStream();
            BufferedInputStream buf = new BufferedInputStream(in);
            byte[] buffer = new byte[1024];
            int iRead;

            while ((iRead = buf.read(buffer)) != -1) {
                json.append(new String(buffer, 0, iRead, StandardCharsets.UTF_8));
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return json.toString();
    }
}
