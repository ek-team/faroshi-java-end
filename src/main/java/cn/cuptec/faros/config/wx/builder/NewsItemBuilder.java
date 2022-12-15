package cn.cuptec.faros.config.wx.builder;

import me.chanjar.weixin.mp.bean.message.WxMpXmlOutNewsMessage;

public class NewsItemBuilder {

    public WxMpXmlOutNewsMessage.Item build(String url, String title, String picUrl, String description) {
        WxMpXmlOutNewsMessage.Item item = new WxMpXmlOutNewsMessage.Item();
        item.setUrl(url);
        item.setTitle(title);
        item.setPicUrl(picUrl);
        item.setDescription(description);
        return item;
    }

}
