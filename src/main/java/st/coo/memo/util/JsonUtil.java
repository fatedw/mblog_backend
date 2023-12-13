package st.coo.memo.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author sivan.wxw
 * @Date 2023/12/12 21:24
 */
@Slf4j
public class JsonUtil {
    // 创建 ObjectMapper 实例，可以重用以提高性能
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 将对象转换为 JSON 字符串
     *
     * @param obj 需要转换的对象
     * @return 转换后的 JSON 字符串
     */
    public static String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("convert object to json error");
        }
        return null;
    }

    /**
     * 将 JSON 字符串转换为指定类型的对象
     *
     * @param jsonStr JSON 字符串
     * @param clazz   目标对象的类
     * @param <T>     目标对象的类型
     * @return 转换后的对象实例
     */
    public static <T> T fromJson(String jsonStr, Class<T> clazz) {
        try {
            return objectMapper.readValue(jsonStr, clazz);
        } catch (JsonProcessingException e) {
            log.error("convert json to object error");
        }
        return null;
    }
}
