package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.common.utils.DesUtil;
import cn.cuptec.faros.config.datascope.DataScope;
import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.controller.base.AbstractBaseController;
import cn.cuptec.faros.entity.*;
import cn.cuptec.faros.service.*;
import cn.cuptec.faros.vo.ProductStockInfoVo;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/productStock")
public class ProductStockController extends AbstractBaseController<ProductStockService, ProductStock> {

    @Resource
    private ProductService productService;
    @Resource
    private LocatorService locatorService;
    @Resource
    private ActivationCodeRecordService activationCodeRecordService;
    @Resource
    private UserService userService;
    @Resource
    private UserRoleService userRoleService;
    @Resource
    private UserController sysUserController;
    @Resource
    private HospitalInfoService hospitalInfoService;
    @Resource
    private LiveQrCodeService liveQrCodeService;
    @Resource
    private ProductStockRelationQrCodeService productStockRelationQrCodeService;
    @Resource
    private ProductStockVersionRecordService productStockVersionRecordService;
    @Resource
    private ProductStockRepairRecordService productStockRepairRecordService;
    @Resource
    private OperationRecordService operationRecordService;
    @Resource
    private DeviceVersionService deviceVersionService;

    /**
     * 生成设备激活码
     */
    @GetMapping("/initActivationCode")
    public RestResponse initActivationCode(@RequestParam("productStockId") int productStockId, @RequestParam("macAdd") String macAdd, @RequestParam("endTime") String endTime, @RequestParam("startTime") String startTime) {
        Assert.notNull(macAdd, "mac地址不能为空");
        Assert.notNull(endTime, "结束时间不能为空");
        service.initActivationCode(macAdd, endTime, 0, startTime, productStockId);
        return RestResponse.ok();
    }

    /**
     * 查询设备激活码记录
     */
    @GetMapping("/queryActivationCode")
    public RestResponse queryActivationCode(@RequestParam("productStockId") int productStockId) {
        LambdaQueryWrapper<ActivationCodeRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ActivationCodeRecord::getProductStockId, productStockId);
        List<ActivationCodeRecord> list = activationCodeRecordService.list(queryWrapper);
        if (CollectionUtils.isEmpty(list)) {
            return RestResponse.ok(list);
        }
        List<Integer> userIds = list.stream().map(ActivationCodeRecord::getCreateId)
                .collect(Collectors.toList());
        List<User> users = (List<User>) userService.listByIds(userIds);
        Map<Integer, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, t -> t));
        for (ActivationCodeRecord codeRecord : list) {
            User user = userMap.get(codeRecord.getCreateId());
            codeRecord.setCreateName(user == null ? null : user.getNickname());
        }
        return RestResponse.ok(list);

    }

    /**
     * 库存管理
     *
     * @return
     */

    @GetMapping("/page")
    public RestResponse pageList() {
        QueryWrapper queryWrapper = getQueryWrapper(getEntityClass());
        Page<ProductStock> page = getPage();
        return RestResponse.ok(service.page(page, queryWrapper));
    }

    @GetMapping("/pageScoped")
    public RestResponse pageScoped() {
        QueryWrapper queryWrapper = getQueryWrapper(getEntityClass());
        Page<ProductStock> page = getPage();
        return RestResponse.ok(service.pageScoped(page, queryWrapper));
    }

    public static boolean isNumericZidai(String str) {
        for (int i = 0; i < str.length(); i++) {
            System.out.println(str.charAt(i));
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    //只能查询本部门及部门以下的设备
    @GetMapping("/pageScopedByDep")
    public RestResponse pageScopedByDep(@RequestParam("search") String search, @RequestParam(required = false, value = "productSn") String productSn, @RequestParam(required = false, value = "hospitalInfo") String hospitalInfo, @RequestParam("sort") String sort, @RequestParam(value = "productId", required = false) String productId
            , @RequestParam(value = "status", required = false) Integer status) {

        RestResponse restResponse = sysUserController.listByDep();
        if (restResponse.isOk()) {
            //判断搜索条件是 手机号还是昵称 或者是 mac地址
            String nickname = null;
            String phone = null;
            String macAdd = null;
            if (!StringUtils.isEmpty(search)) {
                if (search.indexOf(":") > 0) {
                    macAdd = search;
                } else if (isNumericZidai(search)) {
                    phone = search;
                } else {
                    nickname = search;
                }
            }
            List<User> users = (List<User>) restResponse.getData();
            List<Integer> uids = new ArrayList<>();
            QueryWrapper queryWrapper = getQueryWrapper(getEntityClass());

            if (!CollectionUtils.isEmpty(users)) {
                uids = users.stream().map(User::getId)
                        .collect(Collectors.toList());
                queryWrapper.in("salesman_id", uids);
            } else {
                return RestResponse.ok();
            }
            queryWrapper.eq(status != null, "status", status);
            Page<ProductStock> page = getPage();
            //如果是管理员则查看所有
            Integer uid = SecurityUtils.getUser().getId();
            Boolean isAdmin = userRoleService.judgeUserIsAdmin(uid);
            IPage iPage = null;
            if (isAdmin) {
                iPage = service.pageScopedDepAll(page, queryWrapper, nickname, phone, macAdd, productSn, hospitalInfo, sort, productId);

            } else {
                DataScope dataScope = new DataScope();
                dataScope.setIsOnly(true);
                iPage = service.pageScopedDep(page, queryWrapper, nickname, phone, macAdd, productSn, hospitalInfo, sort, productId, dataScope);

            }
            if (iPage != null) {
                List<ProductStock> records = iPage.getRecords();
                if (!CollectionUtils.isEmpty(records)) {
                    //判断是否有绑定纳里二维码
                    List<Integer> uIds = new ArrayList<>();
                    for (ProductStock productStock : records) {
                        if (StringUtils.isEmpty(productStock.getNaniQrCodeUrl())) {
                            productStock.setNaniQrCodeUrl("未绑定");
                        }
                        if (productStock.getUserId() != null) {
                            uIds.add(productStock.getUserId());
                        }
                    }
                    if (!CollectionUtils.isEmpty(uIds)) {
                        List<User> users1 = (List<User>) userService.listByIds(uIds);
                        Map<Integer, List<User>> userMap = users1.stream()
                                .collect(Collectors.groupingBy(User::getId));
                        for (ProductStock productStock : records) {
                            if (productStock.getUserId() != null) {
                                if (!CollectionUtils.isEmpty(userMap.get(productStock.getUserId()))) {
                                    productStock.setUserName(userMap.get(productStock.getUserId()).get(0).getNickname());

                                }
                            }
                        }
                        iPage.setRecords(records);
                    }

                }

            }
            List<ProductStock> records = iPage.getRecords();
            if (!CollectionUtils.isEmpty(records)) {
                List<String> qrcodeIds = records.stream().map(ProductStock::getLiveQrCodeId)
                        .collect(Collectors.toList());
                List<LiveQrCode> liveQrCodes = (List<LiveQrCode>) liveQrCodeService.listByIds(qrcodeIds);
                Map<String, LiveQrCode> liveQrCodeMap = liveQrCodes.stream()
                        .collect(Collectors.toMap(LiveQrCode::getId, t -> t));
                for (ProductStock productStock : records) {
                    if (!StringUtils.isEmpty(productStock.getLiveQrCodeId())) {
                        LiveQrCode liveQrCode = liveQrCodeMap.get(productStock.getLiveQrCodeId());
                        if (liveQrCode != null) {
                            productStock.setUrlName(liveQrCode.getUrlName());

                        }
                    }

                }
                iPage.setRecords(records);
            }
            return RestResponse.ok(iPage);
        }
        return RestResponse.failed("该登录用户没查询到所属部门");
    }

    /**
     * 查看设备历史使用情况
     *
     * @return
     */
    @GetMapping("/pageHistoryRecord")
    public RestResponse pageHistoryRecord(@RequestParam("productStockId") int productStockId) {

        return RestResponse.ok(service.pageHistoryRecord(productStockId));
    }

    @GetMapping("/listAll")
    public RestResponse listAll() {
        QueryWrapper queryWrapper = getQueryWrapper(getEntityClass());
        return RestResponse.ok(service.list(queryWrapper));
    }

    @GetMapping("/listScoped")
    public RestResponse listScoped() {
        QueryWrapper queryWrapper = getQueryWrapper(getEntityClass());
        List<ProductStock> productStocks = service.listScoped1(queryWrapper);
        return RestResponse.ok(productStocks);
    }

    @GetMapping("/listScopedNoParam")
    public RestResponse listScopedNoParam() {
        QueryWrapper queryWrapper = getQueryWrapper(getEntityClass());
        List<ProductStock> productStocks = service.listScopedNoParam(queryWrapper);
        return RestResponse.ok(productStocks);
    }

    //根据仓库id查询设备
    @GetMapping("/queryByLocatorId")
    public RestResponse queryByLocatorId() {
        QueryWrapper queryWrapper = getQueryWrapper(getEntityClass());
        List<ProductStock> productStocks = service.listScoped(queryWrapper);
        return RestResponse.ok(productStocks);
    }

    //查询仓库产品数量
    @GetMapping("/listMyScopedLocator")
    public RestResponse listMyScopedLocator() {
        QueryWrapper queryWrapper = getQueryWrapper(getEntityClass());
        List<ProductStock> productStocks = service.listScoped(queryWrapper);
        if (CollectionUtils.isEmpty(productStocks)) {
            return RestResponse.ok();

        }

        List<Locator> locators = new ArrayList<>();
        Map<Integer, List<ProductStock>> productStockMap = productStocks.stream()
                .collect(Collectors.groupingBy(ProductStock::getLocatorId));
        for (Integer key : productStockMap.keySet()) {
            int num = (productStockMap.get(key).size()) - productStockMap.get(key).get(0).getProductLockNum();
            if (num >= 0) {
                Locator locator = new Locator();
                locator.setLocatorName(productStockMap.get(key).get(0).getLocatorName());
                locator.setProductNum(num);
                locator.setId(productStockMap.get(key).get(0).getLocatorId());
                locators.add(locator);
            }

        }

        return RestResponse.ok(locators);
    }


    //查询仓库产品数量
    @GetMapping("/listScopedLocator")
    public RestResponse listScopedLocator() {
        QueryWrapper queryWrapper = getQueryWrapper(getEntityClass());
        List<ProductStock> productStocks = service.listScoped1(queryWrapper);
        if (CollectionUtils.isEmpty(productStocks)) {
            return RestResponse.ok();

        }

        List<Locator> locators = new ArrayList<>();
        Map<Integer, List<ProductStock>> productStockMap = productStocks.stream()
                .collect(Collectors.groupingBy(ProductStock::getLocatorId));
        for (Integer key : productStockMap.keySet()) {
            int num = (productStockMap.get(key).size()) - productStockMap.get(key).get(0).getProductLockNum();
            if (num >= 0) {
                Locator locator = new Locator();
                locator.setLocatorName(productStockMap.get(key).get(0).getLocatorName());
                locator.setProductNum(num);
                locator.setId(productStockMap.get(key).get(0).getLocatorId());
                locators.add(locator);
            }

        }

        return RestResponse.ok(locators);
    }

    //获取实时库存信息
    @GetMapping("/listScopedStockInfo")
    public RestResponse listScopedStockInfo() {
        QueryWrapper queryWrapper = getQueryWrapper(getEntityClass());
        List<ProductStockInfoVo> productStockInfoVos = service.listScopedStockInfo(queryWrapper);
        return RestResponse.ok(productStockInfoVos);
    }

    /**
     * 查看产品库存
     */
    @GetMapping("/productStock/detail/{stockId}")
    public RestResponse<ProductStock> queryProductStock(@PathVariable String stockId) {
        return RestResponse.ok(service.getById(stockId));
    }

    @GetMapping("/getById")
    public RestResponse getProductStockById(@RequestParam int productStockId) {
        ProductStock productStock = service.getDetailById(productStockId);
        return RestResponse.ok(productStock);
    }


    @GetMapping("/getByMac/{macAddress}")
    public RestResponse getProductStockByMac(@PathVariable String macAddress) {
        ProductStock one = service.getOne(new QueryWrapper<ProductStock>().lambda().eq(ProductStock::getMacAddress, macAddress).eq(ProductStock::getDel, 1));
        if (one == null) {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            one = new ProductStock();
            LocalDate now = LocalDate.now();
            LocalDate endTimeDate = now.plusDays(30);
            one.setActivationDate(endTimeDate);

            String endTime = endTimeDate.format(fmt);

            String[] split = endTime.split("-");
            endTime = split[0].substring(split[0].length() - 2, split[0].length()) + split[1] + split[2];
            try {

                DesUtil des = new DesUtil("productStock");

                //mac地址去除冒号
                String[] split1 = macAddress.split(":");
                String mac = split1[split1.length - 3] + split1[split1.length - 2] + split1[split1.length - 1];

                String code = mac + "-" + endTime;
                String secret = des.encrypt(code);
                one.setActivationCode(secret);

                //存入数据库
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return RestResponse.ok(one);
    }

    @GetMapping("/getByProductSn")
    public RestResponse getByProductSn(@RequestParam("productSn") String productSn) {
        ProductStock one = service.getOne(new QueryWrapper<ProductStock>().lambda().eq(ProductStock::getProductSn, productSn));
        //查询医院
        if (one.getHospitalId() != null) {
            HospitalInfo hospitalInfo = hospitalInfoService.getById(one.getHospitalId());
            one.setHospitalName(hospitalInfo.getName());
        }

        return RestResponse.ok(one);
    }

    @GetMapping("/getByMacAddress")
    public RestResponse getByMacAddress(@RequestParam("macAddress") String macAddress) {

        return RestResponse.ok(service.getOne(new QueryWrapper<ProductStock>().lambda().eq(ProductStock::getMacAddress, macAddress)));
    }

    @PutMapping("/updateById")
    public RestResponse updateById(@RequestBody ProductStock productStock) {
        List<ProductStock> dbProductStock = service.list(Wrappers.<ProductStock>lambdaQuery()
                .nested(query -> query.eq(ProductStock::getProductSn, productStock.getProductSn()).eq(ProductStock::getDel, 1))
                .or(query -> query.eq(ProductStock::getMacAddress, productStock.getMacAddress()).eq(ProductStock::getDel, 1)));
        if(!CollectionUtils.isEmpty(dbProductStock)){
            for(ProductStock productStockd1:dbProductStock){
                if (productStockd1 != null && !productStockd1.getId().equals(productStock.getId())) {
                    throw new RuntimeException("已存在序列号为【" + productStockd1.getProductSn() + "】的产品 或者 mac地址为 [" + productStockd1.getMacAddress() + "] ");
                }

            }
        }
        //添加操作记录
        ProductStock byId = service.getById(productStock.getId());
        OperationRecord operationRecord = new OperationRecord();
        operationRecord.setPathUrl("productStock/updateById");
        operationRecord.setText("编辑设备");
        operationRecord.setUserId(SecurityUtils.getUser().getId() + "");
        operationRecord.setCreateTime(new Date());
        operationRecord.setProductStockId(productStock.getId() + "");
        operationRecord.setMacAdd(productStock.getMacAddress());
        operationRecord.setOldMacAdd(byId.getMacAddress());
        operationRecord.setProductSn(productStock.getProductSn());
        operationRecordService.save(operationRecord);
        //添加编辑记录
        User user = userService.getById(SecurityUtils.getUser().getId());

        ProductStockRepairRecord record=new ProductStockRepairRecord();
        record.setCreateTime(new Date());
        record.setProductStockId(productStock.getId());
        record.setContent("用户ID"+user.getNickname()+""+user.getPhone()+"编辑mac地址从"+byId.getMacAddress()+"改成"+productStock.getMacAddress()+"序列号从"+
                byId.getProductSn()+"改为"+productStock.getProductSn());
        productStockRepairRecordService.save(record);

        //如果locatorId不为空，设置其所属部门
        if (productStock.getLocatorId() != null) {
            Locator locator = locatorService.getById(productStock.getLocatorId());
            productStock.setDeptId(locator.getDeptId());
        }
        //如果业务员id不为空指定业务员所在的部门
        if (productStock.getSalesmanId() != null) {
            User user1 = userService.getById(productStock.getSalesmanId());
            productStock.setDeptId(user1.getDeptId());
        }
        if (StrUtil.isNotEmpty(productStock.getMacAddress())) {
            productStock.setMacAddress(productStock.getMacAddress().toLowerCase());
        }

        service.updateById(productStock);
        return RestResponse.ok();
    }

    @PutMapping("/updateDataById")
    public RestResponse updateDataById(@RequestBody ProductStock productStock) {

        service.updateById(productStock);
        return RestResponse.ok();
    }

    /**
     * 绑定纳迷健康二维码
     *
     * @return
     */
    @PostMapping("/bindNaNiUrl")
    public RestResponse bindNaNiUrl(@RequestBody BindNaNiUrlParam bindNaNiUrlParam) {
        ProductStock productStock = new ProductStock();
        productStock.setId(bindNaNiUrlParam.getProductStockId());
        if (CollectionUtils.isEmpty(bindNaNiUrlParam.getQrCodeIds())) {
            productStock.setNaniQrCodeUrl("未绑定");
            //productStockRelationQrCodeService.remove(new QueryWrapper<ProductStockRelationQrCode>().lambda().eq(ProductStockRelationQrCode::getProductStockId, bindNaNiUrlParam.getProductStockId()));
        } else {
            productStock.setNaniQrCodeUrl("已绑定");
        }
        //先删除之前的
        productStockRelationQrCodeService.remove(new QueryWrapper<ProductStockRelationQrCode>().lambda().eq(ProductStockRelationQrCode::getProductStockId, bindNaNiUrlParam.getProductStockId()));
        List<ProductStockRelationQrCode> productStockRelationQrCodes = new ArrayList<>();
        for (String qrCodeId : bindNaNiUrlParam.getQrCodeIds()) {
            ProductStockRelationQrCode productStockRelationQrCode = new ProductStockRelationQrCode();
            productStockRelationQrCode.setProductStockId(bindNaNiUrlParam.getProductStockId());
            productStockRelationQrCode.setLiveQrCodeId(qrCodeId);
            productStockRelationQrCodes.add(productStockRelationQrCode);
        }
        productStockRelationQrCodeService.saveBatch(productStockRelationQrCodes);


        service.updateById(productStock);
        return RestResponse.ok();
    }

    /**
     * 查询已绑定的纳里二维码
     *
     * @return
     */
    @GetMapping("/queryBingNaLiCode")
    public RestResponse queryBingNaLiCode(@RequestParam("productStockId") Integer productStockId) {
        List<ProductStockRelationQrCode> list = productStockRelationQrCodeService.list(new QueryWrapper<ProductStockRelationQrCode>().lambda().eq(ProductStockRelationQrCode::getProductStockId, productStockId));
        if (CollectionUtils.isEmpty(list)) {
            return RestResponse.ok(new ArrayList<>());
        }
        List<String> liveQrCodeIds = list.stream().map(ProductStockRelationQrCode::getLiveQrCodeId)
                .collect(Collectors.toList());
        List<LiveQrCode> liveQrCodes = (List<LiveQrCode>) liveQrCodeService.listByIds(liveQrCodeIds);
        return RestResponse.ok(liveQrCodes);
    }

    @PutMapping("/updateVersion")
    public RestResponse updateVersion(@RequestBody ProductStock productStock) {

        return RestResponse.ok(service.updateVersion(productStock));
    }

    /**
     * 查询版本列表
     */
    @GetMapping("/queryVersionRecord")
    public RestResponse queryVersionRecord(@RequestParam("id") Integer id) {
        //如果locatorId不为空，设置其所属部门

        List<ProductStockVersionRecord> list = productStockVersionRecordService.list(new QueryWrapper<ProductStockVersionRecord>().lambda().eq(ProductStockVersionRecord::getProductStockId, id));

        return RestResponse.ok(list);
    }

    /**
     * 产品的删除
     *
     * @param id
     * @return
     */
    @DeleteMapping("/deleteById/{id}")
    public RestResponse ProductStockDel(@PathVariable String id) {
        //添加操作记录
        OperationRecord operationRecord = new OperationRecord();
        operationRecord.setPathUrl("productStock/deleteById");
        operationRecord.setText("删除设备");
        operationRecord.setUserId(SecurityUtils.getUser().getId() + "");
        operationRecord.setCreateTime(new Date());
        operationRecordService.save(operationRecord);
        return RestResponse.ok(service.deleteById(id) > 0 ? RestResponse.ok(DATA_DELETE_SUCCESS) : RestResponse.failed(DATA_DELETE_FAILED));
    }

    /**
     * 产品扫码入库
     *
     * @param productId
     * @param qrcodeId
     * @return
     */
    @GetMapping("/scan/inStock/{productId}/{qrcodeId}/{productSn}")
    public RestResponse scanQrCodeInStock(@PathVariable String productId, @PathVariable String qrcodeId, @PathVariable String productSn) {
        //第一步 根据productId 查询产品信息
        Product product = productService.getById(productId);
        //第二步 根据 把产品信息存入表中
        ProductStock productStock = new ProductStock();
        productStock.setLiveQrCodeId(qrcodeId);
        productStock.setProductSn(productSn);
        productStock.setStatus(10);//10表示库存状态
        productStock.setProductId(product.getId());
        productStock.setCreateDate(new Date());
        boolean issuccess = service.save(productStock);
        return RestResponse.ok(issuccess ? RestResponse.ok(DATA_INSERT_SUCCESS) : RestResponse.failed(DATA_INSERT_FAILED));
    }


    //查找当前终端用户的产品列表
    @GetMapping("/listMyProduct")
    public RestResponse listMyProduct() {
        return RestResponse.ok(service.listCustomerProduct());
    }

    @GetMapping("/getBindHospital/{macAddress}")
    public RestResponse getBindHospital(@PathVariable String macAddress) {
        return RestResponse.ok(service.getBindHospital(macAddress));
    }


    @GetMapping("/getOrderDeliverinfoByLiveQrCode")
    public RestResponse getOrderDeliverinfoByLiveQrCode(@RequestParam("liveQrCode") String liveQrCode) {


        ProductStock one = service.getOne(new QueryWrapper<ProductStock>().lambda().eq(ProductStock::getLiveQrCodeId, liveQrCode).last(" limit 1"));
        Assert.notNull(one, "操作失败,设备不存在");
        //Assert.isTrue(one.getStatus() == 20, "操作失败,设备已被使用");
        //User byId = userService.getById(SecurityUtils.getUser().getId());
        //Assert.isTrue(byId != null && one.getDeptId() != null && one.getDeptId().equals(byId.getDeptId()), "操作失败,不是本部门设无法使用");
        return RestResponse.ok(one);
    }

    @GetMapping("/getProductSnAndMacByLiveQrCode")
    public RestResponse getProductSnAndMacByLiveQrCode(@RequestParam("liveQrCode") String liveQrCode) {
        ProductStock one = service.getOne(new QueryWrapper<ProductStock>().lambda().eq(ProductStock::getLiveQrCodeId, liveQrCode).last(" limit 1"));
        return RestResponse.ok(one);
    }

    /**
     * 添加设备备注信息 维修记录
     *
     * @return
     */
    @PostMapping("/addProductRepairRecord")
    public RestResponse addProductRepairRecord(@RequestBody ProductStockRepairRecord record) {
        record.setCreateTime(new Date());
        productStockRepairRecordService.save(record);
        return RestResponse.ok();
    }

    /**
     * 查询设备备注信息 维修记录
     *
     * @return
     */
    @GetMapping("/queryProductRepairRecord")
    public RestResponse queryProductRepairRecord(@RequestParam("id") Integer id) {
        List<ProductStockRepairRecord> list = productStockRepairRecordService.list(new QueryWrapper<ProductStockRepairRecord>().lambda().eq(ProductStockRepairRecord::getProductStockId, id));
        return RestResponse.ok(list);
    }

    /**
     * 修改设备升级版本号
     *
     * @return
     */
    @GetMapping("/updateVersionById")
    public RestResponse updateVersionById(@RequestParam("id") Integer id, @RequestParam("version") Integer version) {
        ProductStock productStock = new ProductStock();
        productStock.setVersion(version);
        productStock.setId(id);
        service.updateById(productStock);
        return RestResponse.ok();
    }

    /**
     * 批量修改设备升级版本号为最新
     * 如果productId 有值 代表根据类型修改为最新版本号 没值 修改所有设备为最新版本号
     *
     * @return
     */
    @GetMapping("/batchUpdateVersion")
    public RestResponse batchUpdateVersion(@RequestParam(value = "productId", required = false) Integer productId, @RequestParam(value = "type", required = false) Integer type) {
        if (productId == null) {
            //修改所有为最新版本号
            List<Integer> deviceVersions = deviceVersionService.groupByType();
            for (Integer deviceVersionType : deviceVersions) {
                if (deviceVersionType != null) {
                    DeviceVersion data = deviceVersionService.newVersion(deviceVersionType);
                    if (data != null) {
                        List<ProductStock> list = service.list(new QueryWrapper<ProductStock>().lambda().eq(ProductStock::getProductId, data.getType()));
                        if (!CollectionUtils.isEmpty(list)) {
                            for (ProductStock productStock : list) {
                                productStock.setVersion(Integer.parseInt(data.getVersion()));
                            }
                            service.updateBatchById(list);
                        }
                    }


                }
            }


            return RestResponse.ok();
        }
        //根据类型修改为最新版本号
        DeviceVersion deviceVersion = deviceVersionService.newVersion(type);
        service.update(Wrappers.<ProductStock>lambdaUpdate()
                .eq(ProductStock::getProductId, productId)
                .set(ProductStock::getVersion, deviceVersion.getVersion())
        );
        return RestResponse.ok();
    }

    @Override
    protected Class<ProductStock> getEntityClass() {
        return ProductStock.class;
    }
}
