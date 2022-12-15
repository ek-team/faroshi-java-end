package cn.cuptec.faros.service;

import cn.cuptec.faros.entity.ActivationCodeRecord;
import cn.cuptec.faros.entity.Article;
import cn.cuptec.faros.mapper.ActivationCodeRecordMapper;
import cn.cuptec.faros.mapper.ArticleMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class ArticleService extends ServiceImpl<ArticleMapper, Article> {
}
