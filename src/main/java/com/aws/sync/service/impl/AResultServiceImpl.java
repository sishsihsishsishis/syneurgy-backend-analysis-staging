package com.aws.sync.service.impl;

import com.aws.sync.vo.csv.AMean;
import com.aws.sync.vo.csv.AllUser;
import com.aws.sync.entity.AResult;
import com.aws.sync.mapper.AResultMapper;
import com.aws.sync.service.AResultService;
import com.aws.sync.vo.DataAVO;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
public class AResultServiceImpl extends ServiceImpl<AResultMapper,AResult> implements AResultService {
    @Resource
    AResultMapper aResultMapper;

    @Autowired
    SqlSessionFactory sqlSessionFactory;

    @Override
    public int insertA(List<AResult> aResults) {
        return aResultMapper.addBatch(aResults);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addOneByOne(List<AResult> aResults) {
        SqlSession session = sqlSessionFactory.openSession(ExecutorType.BATCH);
        AResultMapper am = session.getMapper(AResultMapper.class);
        for(AResult a : aResults){
            am.addResultOneByOne(a);
        }
        session.commit();
        session.close();
    }

    @Override
    public List<AMean> findMean(Long meetingID) {
        return aResultMapper.findMean(meetingID);
    }

    @Override
    public List<AllUser> findUser(Long meetingID) {
        return aResultMapper.findUser(meetingID);
    }

    @Override
    public List<Long> findTime(Long meetingID) {
        return aResultMapper.findTimeline(meetingID);
    }

    @Override
    public List<DataAVO> findData(Long meetingID) {
        return aResultMapper.findData(meetingID);
    }
}
