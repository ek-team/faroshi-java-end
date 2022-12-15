package cn.cuptec.faros.common.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class OrderNumberUtil{
        private static int sequence = 0;
        private static int length = 6;

        public static synchronized String getLocalTrmSeqNum() {
            sequence = sequence >= 999999 ? 1 : sequence + 1;
            String datetime = new SimpleDateFormat("yyyyMMddHHmmss")
                    .format(new Date());
            String s = Integer.toString(sequence);
            return datetime +addLeftZero(s, length);
        }

        /**
         * 左填0
         * @author shijing
         * 2018年6月29日下午1:24:32
         * @param s
         * @param length
         * @return
         */
        public static String addLeftZero(String s, int length) {
            // StringBuilder sb=new StringBuilder();
            int old = s.length();
            if (length > old) {
                char[] c = new char[length];
                char[] x = s.toCharArray();
                if (x.length > length) {
                    throw new IllegalArgumentException(
                            "Numeric value is larger than intended length: " + s
                                    + " LEN " + length);
                }
                int lim = c.length - x.length;
                for (int i = 0; i < lim; i++) {
                    c[i] = '0';
                }
                System.arraycopy(x, 0, c, lim, x.length);
                return new String(c);
            }
            return s.substring(0, length);
        }

    public static void main(String[] args) {
        String localTrmSeqNum = OrderNumberUtil.getLocalTrmSeqNum();
        System.out.print("订单号===="+localTrmSeqNum);
    }

}
