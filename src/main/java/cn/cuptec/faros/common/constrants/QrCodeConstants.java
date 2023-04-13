package cn.cuptec.faros.common.constrants;

public interface QrCodeConstants {

    //调度地址，即二维码内容地址
    String DISPATCHER_URL = "https://pharos3.ewj100.com/liveQrCode/dispatcher/";
    String DISPATCHER_NALI_URL = "https://pharos3.ewj100.com/liveQrCode/dispatcherNaLi/";

    //产品介绍地址,后面跟产品id，及业务员等其他query参数
    String PRODUCT_INTRODUCE_URL = "https://pharos3.ewj100.com/index.html#/product/introduce/";

    //医生信息编辑页面
    String DOCTOR_URL = "https://pharos3.ewj100.com/index.html#/ucenter/doctorInfo/";

    String DEVICE_BIND_USER_URL = "https://pharos3.ewj100.com/index.html#/ucenter/userBind";
    //注册设备用户页面
    String REGISTER_PLAN_USER_URL = "https://pharos3.ewj100.com/index.html#/nali/register";
    //气动设备训练记录页面
    String PNEUMATIC_Record_PAGE_URL ="https://pharos.ewj100.com/record.html#/ucenter/equipmentTraining/index";

    //产品详情地址,后面跟设备序列号
    String HARDWARE_DETAIL_URL = "https://pharos3.ewj100.com/index.html#/hardware/";

    //纳里选择服务包页面
    String NALI_URL = "https://pharos3.ewj100.com/index.html#/nali/index/";
}
