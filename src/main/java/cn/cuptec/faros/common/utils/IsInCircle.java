package cn.cuptec.faros.common.utils;

/**
 * @Description:
 * @Author mby
 * @Date 2020/8/3 15:08
 */
public class IsInCircle  {


    private static double EARTH_RADIUS = 6378138.0;


    private final static double PHARMACY_NEAR=30000.0;


    private static double rad(double d) {
        return d * Math.PI / 180.0;
    }


    /**
     *
     * @param radius  半径
     * @param lat1  纬度
     * @param lng1  经度
     * @param lat2
     * @param lng2
     * @return
     */
    public static boolean isInCircle(double radius, double lat1, double lng1, double lat2, double lng2) {
        double radLat1 = rad(lat1);
        double radLat2 = rad(lat2);
        double a = radLat1 - radLat2;
        double b = rad(lng1) - rad(lng2);
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) + Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(b / 2), 2)));
        s = s * EARTH_RADIUS;
        s = Math.round(s * 10000) / 10000;
        System.out.println(s+"---------------------------------------");
        if (s > radius) {//不在圆上
            return false;
        } else {
            return true;
        }
    }


    /**
     *
     * 判断是否在3000米内
     * @param lat1
     * @param lng1
     * @param lat2
     * @param lng2
     * @return
     */
    public static double distanceNear(double lat1, double lng1, double lat2, double lng2) {
        double radLat1 = rad(lat1);
        double radLat2 = rad(lat2);
        double a = radLat1 - radLat2;
        double b = rad(lng1) - rad(lng2);
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) + Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(b / 2), 2)));
        s = s * EARTH_RADIUS;
        s = Math.round(s * 10000) / 10000;
        if (s>PHARMACY_NEAR){
            return 0;
        }else {
            return s;
        }
    }


    //计算距离
    public static double distance(double lat1, double lng1, double lat2, double lng2) {
        double radLat1 = rad(lat1);
        double radLat2 = rad(lat2);
        double a = radLat1 - radLat2;
        double b = rad(lng1) - rad(lng2);
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) + Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(b / 2), 2)));
        s = s * EARTH_RADIUS;
        s = Math.round(s * 10000) / 10000;
        return s;


    }
}
