package cn.cuptec.faros.service;

import cn.cuptec.faros.entity.TbTrainData;
import cn.cuptec.faros.entity.TbTrainUser;
import cn.cuptec.faros.entity.TbUserTrainRecord;
import cn.cuptec.faros.mapper.TrainDataMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TrainDataService extends ServiceImpl<TrainDataMapper, TbTrainData> {





    public Object listByRecordId(Integer recordId) {
        LambdaQueryWrapper<TbTrainData> eq = new QueryWrapper<TbTrainData>().lambda().eq(TbTrainData::getRecordId, recordId)
                .orderByAsc(TbTrainData::getCreateDate);

        return this.list(eq);
    }

    public List<TbTrainData> listByRecordIds(List<Integer> recordIds) {
        LambdaQueryWrapper<TbTrainData> eq = new QueryWrapper<TbTrainData>().lambda().in(TbTrainData::getRecordId, recordIds)
                .orderByAsc(TbTrainData::getCreateDate);

        return this.list(eq);
    }
}
