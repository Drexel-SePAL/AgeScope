package CommonUtils;

import java.util.regex.Pattern;

public class CommonString {
    public static Pattern pattern = Pattern.compile("adult(s)?( only)?|over \\b(?:1[4-9]|2[0-9])\\b?|under( )?age|age of \\b(?:1[4-9]|2[0-9])\\b|af_num_adults|未成年|(大于)?\\b(?:1[4-9]|2[0-9])\\b岁|年龄|real identity|(photo|valid) identification|ID No|实名认证");
}
