package com.aws.sync.service.impl;

import com.aws.sync.vo.csv.AllUser;
import com.aws.sync.vo.csv.VMean;
import com.aws.sync.entity.VResult;
import com.aws.sync.mapper.VResultMapper;
import com.aws.sync.service.VResultService;
import com.aws.sync.vo.DataVVO;
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
public class VResultServiceImpl extends ServiceImpl<VResultMapper,VResult> implements VResultService {
    @Resource
    VResultMapper vResultMapper;

    @Autowired
    SqlSessionFactory sqlSessionFactory;

    @Override
    public int insertV(List<VResult> vResults) {
        return vResultMapper.addBatch(vResults);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addOneByOne(List<VResult> vResults) {
        SqlSession session = sqlSessionFactory.openSession(ExecutorType.BATCH);
        VResultMapper vm = session.getMapper(VResultMapper.class);
        for(VResult v : vResults){
            vm.addResultOneByOne(v);
        }
        session.commit();
        session.close();
    }

    @Override
    public List<VMean> findMean(Long meetingID) {
        return vResultMapper.findMean(meetingID);
    }

    @Override
    public List<AllUser> findUser(Long meetingID) {
        return vResultMapper.findUser(meetingID);
    }

    @Override
    public List<DataVVO> findData(Long meetingID) {
        return vResultMapper.findData(meetingID);
    }
}
