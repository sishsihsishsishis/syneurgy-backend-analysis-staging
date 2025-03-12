package com.aws.sync.service.impl;

import com.aws.sync.vo.csv.AllUser;
import com.aws.sync.vo.csv.RMean;
import com.aws.sync.entity.RResult;
import com.aws.sync.mapper.RResultMapper;
import com.aws.sync.service.RResultService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
public class RResultServiceImpl extends ServiceImpl<RResultMapper, RResult> implements RResultService {
    @Resource
    RResultMapper rResultMapper;

    @Resource
    SqlSessionFactory sqlSessionFactory;

    @Override
    public int insertR(List<RResult> rResults) {
        return rResultMapper.addBatch(rResults);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addOneByOne(List<RResult> rResults) {
        SqlSession session = sqlSessionFactory.openSession(ExecutorType.BATCH);
        RResultMapper rm = session.getMapper(RResultMapper.class);
        for(RResult r : rResults){
            rm.addResultOneByOne(r);
        }
        session.commit();
        session.close();
    }

    @Override
    public List<RMean> findMean(Long meetingID) {
        return rResultMapper.findMean(meetingID);
    }

    @Override
    public List<AllUser> findUser(Long meetingID) {
        return rResultMapper.findUser(meetingID);
    }
}
