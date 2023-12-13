package st.coo.memo.util;

import org.springframework.util.StringUtils;

/**
 * @Author sivan.wxw
 * @Date 2023/12/13 12:04
 */
public class FileUtil {
    private static final CharSequence[] SPECIAL_SUFFIX = {"tar.bz2", "tar.Z", "tar.gz", "tar.xz"};
    private static final CharSequence[] SPECIAL_SEPARATOR = {"/", "\\"};

    public static String extName(String fileName) {
        if (fileName == null) {
            return null;
        }
        final int index = fileName.lastIndexOf(".");
        if (index == -1) {
            return "";
        } else {
            // issue#I4W5FS@Gitee
            final int secondToLastIndex = fileName.substring(0, index).lastIndexOf(".");
            final String substr = fileName.substring(secondToLastIndex == -1 ? index : secondToLastIndex + 1);
            for (CharSequence specialSuffix : SPECIAL_SUFFIX) {
                if (substr.contains(specialSuffix)) {
                    return substr;
                }
            }

            final String ext = fileName.substring(index + 1);
            // 扩展名中不能包含路径相关的符号
            for (CharSequence separator : SPECIAL_SEPARATOR) {
                if (ext.contains(separator)) {
                    return "";
                }
            }
            return ext;
        }
    }
}
