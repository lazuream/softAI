package com.lazuream.ai.constants;

public class Constants {

    //正则
    public static final String REGEX_PASSWORD = "^(?=.*\\d)(?=.*[a-zA-Z])[\\da-zA-Z~!@#$%^&*_]{8,18}$";
    public static final String ZERO_STR = "0";
    public static final Integer LENGTH_5 = 5;
    public static final Integer LENGTH_10 = 10;
    public static final Integer LENGTH_15 = 15;
    public static final Integer LENGTH_30 = 30;

    public static final String PING = "ping";

    public static final String FILE_FOLDER_FILE = "file/";

    public static final String TOKEN_WEB = "token";


    /**
     * redis key 相关
     */

    /**
     * 过期时间 1分钟
     */
    public static final Long REDIS_KEY_EXPIRES_ONE_MIN = 60L;

    /**
     * 过期时间 1天
     */
    public static final Long REDIS_KEY_EXPIRES_DAY = REDIS_KEY_EXPIRES_ONE_MIN * 60 * 24;

    public static final Integer REDIS_KEY_EXPIRES_HEART_BEAT = 6;

    private static final String REDIS_KEY_PREFIX = "mall:";

    public static final String TOKEN_ADMIN = "adminToken";

    public static final String REDIS_KEY_TOKEN_ADMIN = REDIS_KEY_PREFIX + "token:admin:";

    public static final String REDIS_KEY_CHECK_CODE = REDIS_KEY_PREFIX + "checkcode:";

    public static final String REDIS_KEY_TOKEN_WEB = REDIS_KEY_PREFIX + "token:web:";

    public static final String REDIS_KEY_TOKEN_USERID_WEB = REDIS_KEY_PREFIX + "token:web:userId:";

    public static final String REDIS_KEY_WS_USER_HEART_BEAT = REDIS_KEY_PREFIX + "ws:user:heartbeat";

    //提示词
    public static final String REDIS_KEY_PROMPT = REDIS_KEY_PREFIX + "prompt:";

    public static final String IMAGE_THUMBNAIL_SUFFIX = "_thumbnail";


}
