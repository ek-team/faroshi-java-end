package cn.cuptec.faros.service;

import cn.cuptec.faros.common.utils.DateTimeUtil;
import cn.cuptec.faros.common.utils.DesUtil;
import cn.cuptec.faros.common.utils.StringUtils;
import cn.cuptec.faros.config.datascope.DataScope;
import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.entity.*;
import cn.cuptec.faros.mapper.ProductStockMapper;
import cn.cuptec.faros.vo.ProductStockInfoVo;
import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ProductStockService extends ServiceImpl<ProductStockMapper, ProductStock> {

    @Resource
    private ProductService productService;
    @Resource
    private UserRoleService userRoleService;
    @Resource
    private UserOrdertService userOrdertService;
    @Resource
    private RetrieveOrderService retrieveOrderService;
    @Resource
    private UserService userService;
    @Resource
    private MobileService mobileService;
    @Resource
    private ActivationCodeRecordService activationCodeRecordService;
    @Resource
    private LiveQrCodeService liveQrCodeService;
    @Resource
    private ProductStockVersionRecordService productStockVersionRecordService;
    @Resource
    private HospitalInfoService hospitalInfoService;
    //    /**
//     * 产品库存注册
//     * 返回该产品的二维码
//     * 若产品存在，直接返回二维码
//     * @param productStock
//     */
//    @Transactional
//    public void registeProduct(ProductStock productStock){
//        Product product = productService.getById(productStock.getProductId());
//        Assert.notNull(product, "请选择正确的产品");
//        ProductStock dbStock = getOne(Wrappers.<ProductStock>lambdaQuery().eq(ProductStock::getProductSn, productStock.getProductSn()));
//        String url;
//        if (dbStock != null){
//            url = liveQrCodeService.getFullDispatcherUrl(dbStock.getLiveQrCodeId());
//        }
//        else{
//            //新注册，保存库存及二维码
//            LiveQrCode liveQrCode = new LiveQrCode();
//            liveQrCode.setType(1);
//            liveQrCodeService.save(liveQrCode);
//            productStock.setLiveQrCodeId(liveQrCode.getId());
//            productStock.setStatus(1);
//            baseMapper.insert(productStock);
//            url = liveQrCodeService.getFullDispatcherUrl(liveQrCode.getId());
//        }
//        try {
//            boolean isSuccess = QrCodeUtil.createQrCode(ServletUtils.getResponse().getOutputStream(), "png", url, 400);
//            Assert.isTrue(isSuccess, "产品库存注册失败");;
//        } catch (WriterException e) {
//            log.error("产品库存注册失败", e);
//        } catch (IOException e) {
//            log.error("产品库存注册失败", e);
//        }
//    }

    /**
     * 假删除
     *
     * @return
     */
    public Integer deleteById(String id) {
        ProductStock productStock = new ProductStock();
        productStock.setId(Integer.parseInt(id));
        productStock.setDel(2);
        return baseMapper.updateById(productStock);
    }

    public IPage pageScoped(IPage page, Wrapper wrapper) {
        IPage pageResult = baseMapper.pageScoped(page, wrapper, new DataScope());
        return pageResult;
    }

    public IPage pageScopedDep(IPage page, Wrapper wrapper,String activationDate, String nickname, String phone, String macAdd, String productSn, String hospitalInfo, String sort, String productId, DataScope dataScope) {
        IPage pageResult = baseMapper.pageScopedDep(page, wrapper, dataScope,activationDate, nickname, phone, macAdd, productSn, hospitalInfo, sort, productId);
        return pageResult;
    }

    public IPage pageScopedDepAll(IPage page, Wrapper wrapper,String activationDate, String nickname, String phone, String macAdd, String productSn, String hospitalInfo, String sort, String productId) {
        IPage pageResult = baseMapper.pageScopedDepAll(page, wrapper,activationDate, nickname, phone, macAdd, productSn, hospitalInfo, sort, productId);
        return pageResult;
    }

    @Transactional(rollbackFor = Exception.class)
    public void initActivationCode(String macAdd, String endTime, int month, String startTime, int productStockId) {
        DesUtil des = null;
        LambdaQueryWrapper<ProductStock> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductStock::getId, productStockId);
        queryWrapper.eq(ProductStock::getMacAddress, macAdd);
        ProductStock productStock = getOne(queryWrapper);

        ActivationCodeRecord activationCodeRecord = new ActivationCodeRecord();
        LocalDate endTimeDate = LocalDate.parse(endTime, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        activationCodeRecord.setCodeStartTime(LocalDate.parse(startTime, DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        // LocalDate localDate = beginDateTime.plusMonths(month);
        activationCodeRecord.setCodeEndTime(endTimeDate);
        // DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        productStock.setActivationDate(endTimeDate);
        String[] split = endTime.split("-");
        endTime = split[0].substring(split[0].length() - 2, split[0].length()) + split[1] + split[2];
        try {
            des = new DesUtil("productStock");

            //mac地址去除冒号
            String[] split1 = macAdd.split(":");
            System.out.println(split1.length);
//            if(split1.length != 4 ) throw new RuntimeException("请设置正确的mac地址");
            String mac = split1[split1.length - 3] + split1[split1.length - 2] + split1[split1.length - 1];

            String code = mac + "-" + endTime;
            String secret = des.encrypt(code);
            System.out.println("加密后：" + secret);
            //存入数据库

            productStock.setActivationCode(secret);
            updateById(productStock);
            //添加激活码记录

            activationCodeRecord.setCode(secret);
            activationCodeRecord.setCreateDate(new Date());
            activationCodeRecord.setCreateId(SecurityUtils.getUser().getId());
            activationCodeRecord.setProductStockId(productStockId);
            activationCodeRecordService.save(activationCodeRecord);
            //发送秘钥给用户 短信通知 SMS_222275017
            // mobileService.activeCode(userOrder.getReceiverPhone(), secret);
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("激活码生成失败");
        }

    }

    public static void main(String[] args) {
        String macAdd = "40:45:da:96:b6:52";
        String[] split1 = macAdd.split(":");
        String mac = split1[split1.length - 1] + split1[split1.length - 2] + split1[split1.length - 3];
        System.out.println(mac);
    }

    public List<ProductStockHistory> pageHistoryRecord(int productStockId) {
        ProductStock productStock = this.getById(productStockId);
        Integer productId = productStock.getProductId();

        //查询该设备的预约订单
        LambdaQueryWrapper<UserOrder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserOrder::getStatus, 4);
        List<UserOrder> userOrders = userOrdertService.list(queryWrapper);
        //查询设备的回收单
        LambdaQueryWrapper<RetrieveOrder> retrieveOrderQuery = new LambdaQueryWrapper<>();
        retrieveOrderQuery.eq(RetrieveOrder::getProductSn, productStock.getProductSn());
        retrieveOrderQuery.eq(RetrieveOrder::getStatus, 9);
        List<RetrieveOrder> retrieveOrders = retrieveOrderService.list(retrieveOrderQuery);
        List<ProductStockHistory> userOrderData = new ArrayList<>();
        List<Integer> saleIds = new ArrayList<>();
        //统计数据
        if (!CollectionUtils.isEmpty(userOrders)) {
            for (UserOrder userOrder : userOrders) {
                saleIds.add(userOrder.getSalesmanId());
                saleIds.add(userOrder.getUserId());
                ProductStockHistory productStockHistory = new ProductStockHistory();
                productStockHistory.setType("预售");
                productStockHistory.setTotalBuyNum(productStockHistory.getTotalBuyNum() + 1);
                productStockHistory.setSaleId(userOrder.getSalesmanId());
                productStockHistory.setUserId(userOrder.getUserId());
                userOrderData.add(productStockHistory);
            }
        }
        List<ProductStockHistory> retrieveOrderData = new ArrayList<>();
        if (!CollectionUtils.isEmpty(retrieveOrders)) {
            for (RetrieveOrder retrieveOrder : retrieveOrders) {
                saleIds.add(retrieveOrder.getSalesmanId());
                saleIds.add(retrieveOrder.getUserId());
                ProductStockHistory productStockHistory = new ProductStockHistory();
                productStockHistory.setType("回收");
                productStockHistory.setRetrieveTime(retrieveOrder.getCreateTime());
                productStockHistory.setOperateTime(retrieveOrder.getCreateTime());
                productStockHistory.setTotalRetrieveNum(productStockHistory.getTotalRetrieveNum() + 1);
                productStockHistory.setSaleId(retrieveOrder.getSalesmanId());
                productStockHistory.setUserId(retrieveOrder.getUserId());
                retrieveOrderData.add(productStockHistory);
            }
        }
        if(!CollectionUtils.isEmpty(userOrderData)){
           // userOrderData.stream().sorted(Comparator.comparing(ProductStockHistory::getOperateTime)).collect(Collectors.toList());

        }
        retrieveOrderData.stream().sorted(Comparator.comparing(ProductStockHistory::getOperateTime)).collect(Collectors.toList());
        //预收单和回收单交错排序
        List<ProductStockHistory> result = new ArrayList<>();
        result.addAll(userOrderData);
        if (!CollectionUtils.isEmpty(retrieveOrderData)) {
            int sum = 1;
            for (ProductStockHistory productStockHistory : retrieveOrderData) {
                result.add(sum, productStockHistory);
                sum = sum + 2;
            }
        }

        //查询销售姓名
        if (!CollectionUtils.isEmpty(saleIds)) {
            List<User> users = (List) userService.listByIds(saleIds);
            if (!CollectionUtils.isEmpty(users)) {
                Map<Integer, User> userMap = users.stream()
                        .collect(Collectors.toMap(User::getId, t -> t));
                if (!CollectionUtil.isEmpty(result)) {
                    for (ProductStockHistory productStockHistory : result) {
                        productStockHistory.setProductStockCreateTime(productStock.getCreateDate());
                        if (userMap.get(productStockHistory.getSaleId()) != null) {
                            productStockHistory.setSaleName(userMap.get(productStockHistory.getSaleId()).getNickname());
                        }
                        if (userMap.get(productStockHistory.getUserId()) != null) {
                            productStockHistory.setUserName(userMap.get(productStockHistory.getUserId()).getNickname());
                        }
                    }
                }

            }
        }

        return result;
    }

    /**
     * 查找权限内的库存产品列表(每个设备)
     *
     * @param queryWrapper
     * @return
     */
    public List<ProductStock> listScoped(Wrapper queryWrapper) {
        return baseMapper.listScoped(queryWrapper, new DataScope());
    }

    public List<ProductStock> listScoped1(Wrapper queryWrapper) {
        return baseMapper.listScoped1(queryWrapper);
    }

    public List<ProductStock> listScopedNoParam(QueryWrapper queryWrapper) {
        Boolean isAdmin = userRoleService.judgeUserIsAdmin(SecurityUtils.getUser().getId());
        if (isAdmin) queryWrapper.eq("status", 10);
        else queryWrapper.eq("status", 20);


        return baseMapper.listScopedByStatus(queryWrapper, null);
    }

    /**
     * 查询权限内的产品库存信息
     *
     * @param queryWrapper
     */
    public List<ProductStockInfoVo> listScopedStockInfo(QueryWrapper queryWrapper) {
        List<ProductStock> productStocks = listScoped(queryWrapper);
        List<Integer> productIds = new ArrayList<>();
        productStocks.forEach(productStock -> {
            if (!productIds.contains(productStock.getProductId())) {
                productIds.add(productStock.getProductId());
            }
        });

        List<ProductStockInfoVo> productStockInfoVos = new ArrayList<>();
        productIds.forEach(productId -> {
            List<ProductStock> productStockList = productStocks.stream().filter(productStock -> productStock.getProductId().intValue() == productId).collect(Collectors.toList());
            int totalCount = productStockList.size();
            int inStockCount = (int) productStockList.stream().filter(productStock -> productStock.getStatus() == 20).count();
            int saleLockCount = (int) productStockList.stream().filter(productStock -> productStock.getStatus() == 21).count();
            int saledCount = (int) productStockList.stream().filter(productStock -> productStock.getStatus() == 30).count();
            int retrievingCount = (int) productStockList.stream().filter(productStock -> productStock.getStatus() == 31).count();
            ProductStock productStock = productStockList.get(0);
            ProductStockInfoVo productStockInfoVo = new ProductStockInfoVo();
            productStockInfoVo.setProductName(productStock.getProductName());
            productStockInfoVo.setProductPic(productStock.getProductPic());
            productStockInfoVo.setProductType(productStock.getProductType());
            productStockInfoVo.setSalesmanInStockCount(inStockCount);
            productStockInfoVo.setSalesmanSaleLockCount(saleLockCount);
            productStockInfoVo.setSalesmanSaledCount(saledCount);
            productStockInfoVo.setSalesmanRetrievingCount(retrievingCount);
            productStockInfoVo.setTotalCount(totalCount);
            productStockInfoVos.add(productStockInfoVo);
        });
        return productStockInfoVos;
    }

    public void unbindQrcode(String liveQrCodeId) {
        baseMapper.unbindQrcode(liveQrCodeId);
    }

    public ProductStock getproductStockByCodeId(String liveQrCodeId) {
        return baseMapper.getproductStockByCodeId(liveQrCodeId);
    }

    public List<ProductStock> listCustomerProduct() {
        return baseMapper.listCustomerProduct(SecurityUtils.getUser().getId());
    }

    /**
     * 获取设备详情
     *
     * @param id
     * @return
     */
    public ProductStock getDetailById(int id) {
        ProductStock productStock = baseMapper.getDetailById(id);
        //设置当前用户已经用了多久
        if (productStock.getUserId() != null && productStock.getLastBindUserTime() != null) {
            productStock.setYearMonthDayCount(DateTimeUtil.getYearMonthDayDiff(productStock.getLastBindUserTime(), new Date()));
        }
        Object productDetail = productService.getProductDetail(productStock.getProductId(), productStock.getSalesmanId());
        productStock.setProductInfo(productDetail);
        return productStock;
    }

    public void clearOrderUid(Integer id) {
        baseMapper.clearOrderUid(id);
    }

    public ProductStock updateVersion(ProductStock productStock) {
        ProductStock one = this.getOne(Wrappers.<ProductStock>lambdaQuery().eq(ProductStock::getProductSn, productStock.getProductSn()).eq(ProductStock::getDel, 1)
                .last(" limit 1"));
        if (one == null) {
            //新增一个产品
            if (!StringUtils.isEmpty(productStock.getMacAddress())) {
                String replace = productStock.getMacAddress().replace("：", ":");
                productStock.setMacAddress(replace.toLowerCase());
            }
            LiveQrCode liveQrCode = new LiveQrCode();
            liveQrCode.setMacAddress(productStock.getMacAddress());
            liveQrCode.setProductSn(productStock.getProductSn());
            liveQrCode.setType(5);
            liveQrCode.setCurrentUserId(productStock.getCurrentUserId());
            liveQrCode.setProductProductionDate(new Date());
            liveQrCode.setIccId(productStock.getIccId());
            liveQrCode.setSystemVersion(productStock.getSystemVersion());
            liveQrCode.setVersionStr(productStock.getVersionStr());
            String productDeviceType = productStock.getProductDeviceType();
            Integer productId = 1;
            if (!StringUtils.isEmpty(productDeviceType) && productDeviceType.equals("下肢医用版")) {
                productId = 1;
            }
            if (!StringUtils.isEmpty(productDeviceType) &&productDeviceType.equals("下肢家用版")) {
                productId = 2;
            }
            if (!StringUtils.isEmpty(productDeviceType) &&productDeviceType.equals("气动医用版")) {
                productId = 13;
            }
            if (!StringUtils.isEmpty(productDeviceType) &&productDeviceType.equals("气动家用版")) {
                productId = 14;
            }
            liveQrCode.setProductId(productId);
            liveQrCode.setIpAdd(productStock.getIpAdd());
            one = liveQrCodeService.saveProductStock(liveQrCode);

        } else {
            LambdaUpdateWrapper wrapper = new UpdateWrapper<ProductStock>().lambda()
                            .set(ProductStock::getIpAdd, productStock.getIpAdd())
                            .eq(ProductStock::getId, one.getId());
                    this.update(wrapper);
            if(!StringUtils.isEmpty(productStock.getMacAddress())){
                if(!productStock.getMacAddress().equals(one.getMacAddress())){
//                    LambdaUpdateWrapper wrapper = new UpdateWrapper<ProductStock>().lambda()
//                            .set(ProductStock::getMacAddress, productStock.getMacAddress())
//                            .eq(ProductStock::getId, one.getId());
//                    this.update(wrapper);
                }
            }

        }


        //判断版本号是否和之前一样

        if (one != null && !StringUtils.isEmpty(one.getVersionStr()) && !StringUtils.isEmpty(productStock.getVersionStr())) {
            if (!one.getVersionStr().equals(productStock.getVersionStr())) {
                //记录一下版本
                ProductStockVersionRecord record = new ProductStockVersionRecord();
                record.setVersionStr(productStock.getVersionStr());
                record.setCreateTime(new Date());
                record.setProductStockId(one.getId());
                productStockVersionRecordService.save(record);


            }

        }
        return this.getOne(new QueryWrapper<ProductStock>().lambda().eq(ProductStock::getMacAddress, productStock.getMacAddress()).eq(ProductStock::getDel, 1));
    }

    public HospitalInfo getBindHospital(String macAddress) {
        ProductStock one = this.getOne(new QueryWrapper<ProductStock>().lambda().eq(ProductStock::getMacAddress, macAddress));
        if (one == null || one.getHospitalId() == null) return null;
        return hospitalInfoService.getById(one.getHospitalId());
    }

    public ProductStock getByMac(String macAddress) {
        return this.getOne(new QueryWrapper<ProductStock>().lambda().eq(ProductStock::getMacAddress, macAddress).eq(ProductStock::getDel,1).last(" limit 1"));

    }
}
