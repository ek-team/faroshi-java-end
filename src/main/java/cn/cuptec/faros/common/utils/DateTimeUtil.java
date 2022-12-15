package cn.cuptec.faros.common.utils;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


/**
 * @description 时间日期的工具类
 */
public class DateTimeUtil {
	
	private static final Logger logger = LoggerFactory.getLogger(DateTimeUtil.class);
	private static final String DEFAULT_DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";
	private static final String DEFAULT_TIME_PATTERN = "HH:mm:ss";

    /**
     * 获取当天的字符串
     * @return
     */
    public static String getTodayStr(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");
        return sdf.format(new Date());
    }

    public static String getTodayStr2(){
        SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd");
        return sdf.format(new Date());
    }

	public static String getCurrentTime(){
		return getCurrentTime(DEFAULT_TIME_PATTERN);
	}


    /**
     * 获取当前时间的字符串
     * @return
     */
    public static String getCurrentDateTime(){
    	return getCurrentTime(DEFAULT_DATE_PATTERN);
    }
    
    /**
     * 获取当前时间的字符串
     * @param format 字符串格式，如：yy-MM-dd HH:mm:ss
     * @return
     */
    public static String getCurrentTime(String format){
    	SimpleDateFormat sdf = new SimpleDateFormat(format);
    	Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    	return sdf.format(timestamp);
    }
    
    /**
     * 获取当前的月份
     * @return
     */
    public static String getCurrentMonth(){
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
        return sdf.format(new Date());
    }

    /**
     * 比较两个时间，如果返回大于0，time1大于time2,如果返回-1，time1小于time2，返回0则相等
     * @param time1
     * @param time2
     * @return
     * @throws ParseException
     */
    public static int compareTime(String time1,String time2) throws ParseException{
    	SimpleDateFormat sdf = new SimpleDateFormat(DEFAULT_DATE_PATTERN);
    	Date date1 = sdf.parse(time1);
    	Date date2 = sdf.parse(time2);
    	long result = date1.getTime() - date2.getTime();
    	if(result > 0){
    		return 1;
    	}else if(result==0){
    		return 0;
    	}else{
    		return -1;
    	}
    }
    
    /**
     * 转换字符串成日期对象
     * @param dateStr 日期字符串
     * @param format 格式，如：yy-MM-dd HH:mm:ss
     * @return
     */
    public static Date convertStrToDate(String dateStr,String format){
    	if(!StringUtils.isBlank(dateStr)&&!StringUtils.isBlank(format)){
    		try{
	    		SimpleDateFormat sdf = new SimpleDateFormat(format);
	    		return sdf.parse(dateStr);
	    	}catch (Exception e) {
	    		logger.warn("convertDate fail, date is "+ dateStr, e);
			}
    	}
    	return null;
    }
    
    /**
     * 把字符串日期转换成另一种格式
     * @param dateStr 字符串日期
     * @param format 转换日期格式
     * @param otherFormat 转换日期格式
     * @return
     */
    public static String convertDate(String dateStr,String format,String otherFormat){
    	try{
	    	Date date = convertStrToDate(dateStr, format);
			SimpleDateFormat sdf = new SimpleDateFormat(otherFormat);
			return sdf.format(date);
    	}catch (Exception e) {
    		logger.warn("convertDate fail, date is "+ dateStr, e);
		}
    	return null;
    }
    
    /**
     * 把字符串日期转换成另一种格式
     * @param dateStr 字符串日期
     * @param format 转换格式
     * @return
     */
    public static String convertDate(String dateStr,String format){
    	return convertDate(dateStr, DEFAULT_DATE_PATTERN,format);
    }

	/**
	 * 获取时间天数间隔
	 */
	public static int getDayInterval(Date fromDate, Date toDate){
		int minutes = getSubtractMinutes(fromDate, toDate);
		double useDaysCount = Math.ceil((double) minutes / (60 * 24));
		return (int)useDaysCount;
	}

	/**
	 * 获取两个日期相差的月数
	 */
	public static int getMonthDiff(Date d1, Date d2) {
		Calendar c1 = Calendar.getInstance();
		Calendar c2 = Calendar.getInstance();
		c1.setTime(d1);
		c2.setTime(d2);
		int year1 = c1.get(Calendar.YEAR);
		int year2 = c2.get(Calendar.YEAR);
		int month1 = c1.get(Calendar.MONTH);
		int month2 = c2.get(Calendar.MONTH);
		int day1 = c1.get(Calendar.DAY_OF_MONTH);
		int day2 = c2.get(Calendar.DAY_OF_MONTH);
		// 获取年的差值
		int yearInterval = year1 - year2;
		// 如果 d1的 月-日 小于 d2的 月-日 那么 yearInterval-- 这样就得到了相差的年数
		if (month1 < month2 || month1 == month2 && day1 < day2) {
			yearInterval--;
		}
		// 获取月数差值
		int monthInterval = (month1 + 12) - month2;
		if (day1 < day2) {
			monthInterval--;
		}
		monthInterval %= 12;
		int monthsDiff = Math.abs(yearInterval * 12 + monthInterval);
		return monthsDiff;
	}

	/**
	 * 获取两个日期的年月日相差
	 */
	public static YearMonthDayCount getYearMonthDayDiff(Date dateBegin, Date dateEnd) {
		Calendar cBegin = Calendar.getInstance();
		Calendar cEnd = Calendar.getInstance();
		cBegin.setTime(dateBegin);
		cEnd.setTime(dateEnd);
		int yearBegin = cBegin.get(Calendar.YEAR);
		int yearEnd = cEnd.get(Calendar.YEAR);
		int monthBegin = cBegin.get(Calendar.MONTH);
		int monthEnd = cEnd.get(Calendar.MONTH);
		int dayBegin = cBegin.get(Calendar.DAY_OF_MONTH);
		int dayEnd = cEnd.get(Calendar.DAY_OF_MONTH);
		// 获取年的差值
		int yearInterval = yearEnd - yearBegin;
		// 如果 d1的 月-日 小于 d2的 月-日 那么 yearInterval-- 这样就得到了相差的年数
		if (monthEnd < monthBegin || monthEnd == monthBegin && dayEnd < dayBegin)
			yearInterval--;
		// 获取月数差值
		int monthInterval = (monthEnd + 12) - monthBegin;
		if (dayEnd < dayBegin)
			monthInterval--;
		monthInterval %= 12;
		//获取天数差值
		int dayDiff;
		int dayCount = getDaysOfMonth(dateBegin);
		dayDiff = dayCount - dayBegin + dayEnd;
		if (dayDiff >= dayCount){
			dayDiff = dayDiff - dayCount;
		}

		YearMonthDayCount yearMonthDayCount = new YearMonthDayCount();
		yearMonthDayCount.setYear(yearInterval);
		yearMonthDayCount.setMonth(monthInterval);
		yearMonthDayCount.setDay(dayDiff);

		if (yearInterval == 0 && monthInterval == 0 && dayDiff == 0){
			yearMonthDayCount.setDay(1);
		}
		return yearMonthDayCount;
	}

	/**
	 * 计算时间的分钟差值 toDate - fromDate
	 * @param fromDate 减数
	 * @param toDate 被减数
	 * @return
	 */
	public static int getSubtractMinutes(Date fromDate, Date toDate){
		if (fromDate != null && toDate != null){
			long d1 = fromDate.getTime();
			long d2 = toDate.getTime();
			return (int)Math.ceil(Math.ceil((double)((double)d2-(double)d1)/(1000 * 60)));
		}
		return 0;
	}

	//获取当月有多少天
	public static int getDaysOfMonth(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
	}

	@Data
	public static class YearMonthDayCount{
		private int year;
		private int month;
		private int day;
	}

}
