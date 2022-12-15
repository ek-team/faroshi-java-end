package cn.cuptec.faros.vo;

import cn.cuptec.faros.entity.UserOrder;
import lombok.Data;

import java.util.List;

@Data
public class MapExpressTrackVo {

    private Integer status;

    private String message;

    private String nu;

    private Integer state;

    private String com;

    private String trailUrl;

    private String arrivalTime;

    private String totalTime;

    private String remainTime;

    private Object userOrder;

    private ExpressData[] data;

    private ExpressRouteInfo routeInfo;

    @Data
    public static class ExpressData{
        private String time;

        private String context;

        private String ftime;

        private String areaCode;

        private String areaName;

        private String status;
    }

    @Data
    public static class ExpressRouteInfo{

        private RouteInfo from;

        private RouteInfo cur;

        private RouteInfo to;

        @Data
       public static class RouteInfo{
           private String number;

           private String name;
       }

    }

    @Data
    public static class ExpressParam{
        private String com;

        private String num;

        private String order = "desc";

        private String from;

        private String to;

        private String phone = "";

        private String show = "0";

        private String orderTime = "";
    }

}
