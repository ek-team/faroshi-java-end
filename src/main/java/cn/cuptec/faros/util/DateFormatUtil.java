package cn.cuptec.faros.util;

import cn.cuptec.faros.common.utils.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class DateFormatUtil {
    /**
     *
     * @param time  1541569323155
     * @param pattern yyyy-MM-dd HH:mm:ss
     * @return 2018-11-07 13:42:03
     */
    public static String getDate2String(long time, String pattern) {
        if(pattern == null){
            pattern = "yyyy-MM-dd HH:mm:ss";
        }
        Date date = new Date(time);
        SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.getDefault());
        return format.format(date);
    }
    public static String getNowDate() {
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return format.format(date);
    }

    /**
     *
     * @param dateString 2018-11-07 13:42:03,
     * @param pattern yyyy-MM-dd HH:mm:ss
     * @return 1541569323000
     */
    public static long getString2Date(String dateString, String pattern) {

        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern, Locale.getDefault());
        Date date = new Date();
        try {
            date = dateFormat.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date.getTime();
    }
    /**
     *
     * @param dateString 2018-11-07 13:42:03,
     * @return 1541569323000
     */
    public static long getString2Date(String dateString) {
        if (StringUtils.isEmpty(dateString)){
            return 0;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        try {
            date = dateFormat.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date.getTime();
    }

    public static String getString2DateIncreaseOneDay(String dateString, String pattern) {
        if(pattern == null){
            pattern = "yyyy-MM-dd HH:mm:ss";
        }
        long transformDate = getString2Date(dateString) + 1000;
        Date date = new Date(transformDate);
        SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.getDefault());
        return format.format(date);
    }
    public static String increaseOneDayOneSecondLess(int distanceDay,String currentTime) {
        String dateString = getBeforeOrAfterDate(distanceDay,currentTime);
        String pattern = "yyyy-MM-dd HH:mm:ss";
        long transformDate = getString2Date(dateString) + 24 * 60 * 60 * 1000 - 1000;
        Date date = new Date(transformDate);
        SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.getDefault());
        return format.format(date);
    }
    public static long getSpecialString2Date(String dateString) {
        String tempDate = "20" + dateString + " 23:59:59";

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        try {
            date = dateFormat.parse(tempDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date.getTime();
    }
    /**
     * 获取输入的日期 前n天日期、后n天日期
     *
     * @param distanceDay 前几天 如获取前7天日期则传-7即可；如果后7天则传7
     * @return
     */
    public static String getBeforeOrAfterDate(int distanceDay,String currentTime) {
        long time = getString2Date(currentTime,"yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat dft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.getDefault());
        Date beginDate = new Date(time);
        Calendar date = Calendar.getInstance();
        date.setTime(beginDate);
        date.set(Calendar.DATE, date.get(Calendar.DATE) + distanceDay);
        Date endDate = null;
        try {
            endDate = dft.parse(dft.format(date.getTime()));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dft.format(endDate);
    }
    public static String getWeek(long time) {

        Calendar cd = Calendar.getInstance();
        cd.setTime(new Date(time));

        int year  = cd.get(Calendar.YEAR); //获取年份
        int month = cd.get(Calendar.MONTH); //获取月份
        int day   = cd.get(Calendar.DAY_OF_MONTH); //获取日期
        int week  = cd.get(Calendar.DAY_OF_WEEK); //获取星期

        String weekString;
        switch (week) {
            case Calendar.SUNDAY:
                weekString = "周日";
                break;
            case Calendar.MONDAY:
                weekString = "周一";
                break;
            case Calendar.TUESDAY:
                weekString = "周二";
                break;
            case Calendar.WEDNESDAY:
                weekString = "周三";
                break;
            case Calendar.THURSDAY:
                weekString = "周四";
                break;
            case Calendar.FRIDAY:
                weekString = "周五";
                break;
            default:
                weekString = "周六";
                break;

        }

        return weekString;
    }
    public static long getDateDiff(String startDate,String endDate) {
        long start = getString2Date(startDate);
        long end = getString2Date(endDate);
        return end - start;

    }
    public static String getDay(long time) {
        Calendar cd = Calendar.getInstance();
        cd.setTime(new Date(time));
        int day = cd.get(Calendar.DAY_OF_MONTH); //获取日期
        return String.valueOf(day);
    }
    public static String getDay(String date) {
        long time = getString2Date(date);
        Calendar cd = Calendar.getInstance();
        cd.setTime(new Date(time));
        int day = cd.get(Calendar.DAY_OF_MONTH); //获取日期
        int month = cd.get(Calendar.MONTH) + 1;
        String monthStr;
        String dayStr;
        if (month < 10){
            monthStr = "0" + month;
        }else {
            monthStr = month + "";
        }
        if (day < 10){
            dayStr = "0" + day;
        }else {
            dayStr = day + "";
        }

        return monthStr + "-" + dayStr;
    }
    public static String getZhMonth(String month){
        String zhMonth;
        switch (month){
            case "01月":
                zhMonth = "一月份";
                break;
            case "02月":
                zhMonth = "二月份";
                break;
            case "03月":
                zhMonth = "三月份";
                break;
            case "04月":
                zhMonth = "四月份";
                break;
            case "05月":
                zhMonth = "五月份";
                break;
            case "06月":
                zhMonth = "六月份";
                break;
            case "07月":
                zhMonth = "七月份";
                break;
            case "08月":
                zhMonth = "八月份";
                break;
            case "09月":
                zhMonth = "九月份";
                break;
            case "10月":
                zhMonth = "十月份";
                break;
            case "11月":
                zhMonth = "十一月份";
                break;
            default:
                zhMonth = "十二月份";
                break;
        }
        return zhMonth;
    }

    public static int getDay() {
        return Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
    }

    public static int getYear() {
        return Calendar.getInstance().get(Calendar.YEAR);
    }

    public static int getMonth() {
        return Calendar.getInstance().get(Calendar.MONTH) + 1;
    }

    public static List<String> getMonths() {
        ArrayList list = new ArrayList();
        for (int i = 1; i < 13; i++) {
            list.add(String.valueOf(i));
        }
        return list;
    }

    public static ArrayList<String> getDayByMonth(int yearParam, int monthParam) {
        ArrayList<String> list = new ArrayList<>();
        Calendar aCalendar = Calendar.getInstance(Locale.CHINA);
        aCalendar.set(yearParam, monthParam, 1);
        int year = aCalendar.get(Calendar.YEAR);//年份
        int month = aCalendar.get(Calendar.MONTH) + 1;//月份
        int day = aCalendar.getActualMaximum(Calendar.DATE);
        for (int i = 1; i <= day; i++) {
            String aDate = null;
            if (month < 10 && i < 10) {
                aDate = year + "-0" + month + "-0" + i;
            }
            if (month < 10 && i >= 10) {
                aDate = year + "-0" + month + "-" + i;
            }
            if (month >= 10 && i < 10) {
                aDate = year + "-" + month + "-0" + i;
            }
            if (month >= 10 && i >= 10) {
                aDate = year + "-" + month + "-" + i;
            }
            list.add(aDate);
        }
        return list;
    }


    public static String getTime(int time) {
        long hours = time / (60 * 60);
        long minutes = (time - hours * (60 * 60)) / 60;
        long seconds = time - hours * (60 * 60) - minutes * 60;
        return new StringBuffer().append(hours >= 10 ? hours : "0" + hours).append(":")
                .append(minutes >= 10 ? minutes : "0" + minutes).append(":")
                .append(seconds >= 10 ? seconds : "0" + seconds).toString();
    }

    public static String getTime_(int time) {
        long hours = time / (60 * 60);
        long minutes = (time - hours * (60 * 60)) / 60;
        long seconds = time - hours * (60 * 60) - minutes * 60;
        String result = (hours >= 10 ? hours : "0" + hours) + ":" + (minutes >= 10 ? minutes : "0" + minutes) + ":" +
                (seconds >= 10 ? seconds : "0" + seconds);
        return result;
    }
    public static String getMinuteTime(int time) {
        long minutes = time / 60;
        long seconds = time - minutes * 60;
        String result = (minutes >= 10 ? minutes : "0" + minutes) + ":" +
                (seconds >= 10 ? seconds : "0" + seconds);
        return result;
    }
    public static int[] getMinuteTimes(int time) {
        int[] times = new int[4];
        int minutes = time / 60;
        int seconds = time - minutes * 60;
        times[0] = minutes/10;
        times[1] = minutes%10;
        times[2] = seconds/10;
        times[3] = seconds%10;
        return times;
    }

    public static String getTime(Date date) {//可根据需要自行截取数据显示
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM");
        return format.format(date);
    }

    public static String getMonth(Date date) {//可根据需要自行截取数据显示
        SimpleDateFormat format = new SimpleDateFormat("MM");
        return format.format(date);
    }
    private static long lastClickTime;

    public static boolean setTimeInterval(int INTERVAL) {
        boolean flag = false;
        long curClickTime = System.currentTimeMillis();
        if (Math.abs(curClickTime - lastClickTime) >= INTERVAL) {
            flag = true;
            lastClickTime = curClickTime;
        }
        return flag;
    }
    private static long lastTime = 0;

    public static boolean setRunInterval(int INTERVAL) {
        long curTime = System.currentTimeMillis();
        boolean flag = false;
        if (Math.abs(curTime - lastTime) >= INTERVAL) {
            flag = true;
            lastTime = curTime;
        }
        return flag;
    }
    private static long lastTime1 = 0;

    public static boolean avoidFastClick(int INTERVAL) {
        long curTime = System.currentTimeMillis();
        if (Math.abs(curTime - lastTime1) < INTERVAL) {
            return false;
        }
        lastTime1 = curTime;
        return true;
    }
    private static long lastTime3 = 0;
    public static boolean avoidFastClick2(int INTERVAL) {
        long curTime = System.currentTimeMillis();
        if (Math.abs(curTime - lastTime3) < INTERVAL) {
            return false;
        }
        lastTime3 = curTime;
        return true;
    }
    private static long lastTime2 = 0;
    public static boolean getFastClick(int INTERVAL) {
        long curTime = System.currentTimeMillis();
        if (Math.abs(curTime - lastTime2) < INTERVAL) {
            return true;
        }else {
            lastTime2 = curTime;
        }
        return false;
    }
    public static String secondToMinute(int second){
        String m;
        String s1;
        int s = second % 60;
        if (s >= 10){
            s1 = s + "";
        }else {
            s1 = "0" + s;
        }
        int temp = second / 60;
        if (temp >= 10){
            m = temp + "";
        }else {
            m = "0" + temp;
        }
        return m +":" + s1;
    }
//    public static String getNowDate() {
//        return DateTime.now().toString("yyyy-MM-dd HH:mm:ss");
//    }
//    public static String getString(String time) {
//        return DateTime.parse(time, DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")).toString("yyyy-MM-dd");
//    }
    /**
     * 计算传入时间距离当前时间多久
     *
     * @param date
     * @return
     */
    public static int getTimeDiff(String date) {
        if (StringUtils.isEmpty(date)) {
            return 0;
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        StringBuilder sb = new StringBuilder();
        try {
            Date parse = dateFormat.parse(date);
            Date now = new Date();
            long diff = now.getTime() - parse.getTime();
            // 只能精确到日 无法具细到年 月 不能确定一个月具体多少天 不能确定一年具体多少天
            // 获取日
            long day = diff / (1000 * 60 * 60 * 24);
            diff = diff % (1000 * 60 * 60 * 24);
            if (day > 0) {
                sb.append(day).append("天");
                return (int) day;
            }else {
                return 0;
            }
//            // 获取时
//            long hour = diff / (1000 * 60 * 60);
//            diff = diff % (1000 * 60 * 60);
//            if (hour > 0) {
//                sb.append(hour).append("时");
//            }
//            // 获取分
//            long min = diff / (1000 * 60);
//            diff = diff % (1000 * 60);
//            if (min > 0) {
//                sb.append(min).append("分");
//            }
//            // 获取秒
//            long sec = diff / 1000;
//            if (sec > 0) {
//                sb.append(sec).append("秒");
//            }
//            return sb.toString();
        } catch (ParseException e) {
            return 0;
        }
    }
}
