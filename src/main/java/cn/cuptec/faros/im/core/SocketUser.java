package cn.cuptec.faros.im.core;

import cn.cuptec.faros.entity.User;
import lombok.Data;

import java.util.List;
import java.util.Objects;

/**
 * 长连接用户
 */
@Data
public class SocketUser {


    private boolean auth = false;

    /**
     * 用户地址
     */
    private String addr;

    /**
     * 最后活动时间
     */
    private Long time;

    /**
     * 用户组
     */
    private String group;

    /**
     * 系统用户
     */
    private User userInfo;
    private String macAdd;//mac地址认证


    /**
     * 用户id相同，代表同一个用户
     *
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        SocketUser that = (SocketUser) o;
        return Objects.equals(userInfo.getId(), that.userInfo.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), userInfo.getId());
    }

    @Override
    public String toString() {
        return "SocketUser{" +
                "auth=" + auth +
                ", addr='" + addr + '\'' +
                ", time=" + time +
                ", group='" + group + '\'' +
                ", tenantUser=" + userInfo +
                '}';
    }
}
