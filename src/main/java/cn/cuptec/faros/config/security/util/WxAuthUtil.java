package cn.cuptec.faros.config.security.util;

import java.nio.charset.StandardCharsets;

public class WxAuthUtil {

    public static String encodeURIComponent(String input) {
        if (org.apache.commons.lang3.StringUtils.isEmpty(input)) {
            return input;
        } else {
            int l = input.length();
            StringBuilder o = new StringBuilder(l * 3);

            for(int i = 0; i < l; ++i) {
                String e = input.substring(i, i + 1);
                if (!"abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_.!~*'()".contains(e)) {
                    byte[] b = e.getBytes(StandardCharsets.UTF_8);
                    o.append(getHex(b));
                } else {
                    o.append(e);
                }
            }

            return o.toString();
        }
    }

    private static String getHex(byte[] buf) {
        StringBuilder o = new StringBuilder(buf.length * 3);
        byte[] var2 = buf;
        int var3 = buf.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            byte aBuf = var2[var4];
            int n = aBuf & 255;
            o.append("%");
            if (n < 16) {
                o.append("0");
            }

            o.append(Long.toString((long)n, 16).toUpperCase());
        }

        return o.toString();
    }

}
