package uw.notify.center.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 描述: json操作工具类
 */
public class NotifyJsonUtils {

    private static final Logger logger = LoggerFactory.getLogger( NotifyJsonUtils.class );

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * String -> 对象
     *
     * @param data
     * @param valueType
     * @param <T>
     * @return
     */
    public static <T> T parseObject(byte[] data, Class<T> valueType) {
        try {
            return OBJECT_MAPPER.readValue( data, valueType );
        } catch (Exception e) {
            logger.error( "JsonUtils parseObject error params:{}, message: {}", new String( data ), e.getMessage(), e );
        }
        return null;
    }


    /**
     * 对象 -> String
     *
     * @param value
     * @return
     */
    public static String toJSONString(Object value) {
        try {
            return OBJECT_MAPPER.writeValueAsString( value );
        } catch (Exception e) {
            logger.error( "JsonUtils toJSONString error params:{}, message: {}", value, e.getMessage(), e );
        }
        return null;
    }

}
