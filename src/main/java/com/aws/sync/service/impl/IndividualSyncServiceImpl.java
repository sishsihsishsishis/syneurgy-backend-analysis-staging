package com.aws.sync.service.impl;

import com.aws.sync.entity.IndividualSync;
import com.aws.sync.entity.RResult;
import com.aws.sync.mapper.IndividualSyncMapper;
import com.aws.sync.mapper.RResultMapper;
import com.aws.sync.service.IndividualSyncService;
import com.aws.sync.vo.IndividualAllVO;
import com.aws.sync.vo.IndividualVO;
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
public class IndividualSyncServiceImpl extends ServiceImpl<IndividualSyncMapper, IndividualSync> implements IndividualSyncService {
    @Autowired
    SqlSessionFactory sqlSessionFactory;

    @Resource
    IndividualSyncMapper individualSyncMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addOneByOne(List<IndividualSync> individualSyncAll) {
        SqlSession session = sqlSessionFactory.openSession(ExecutorType.BATCH);
        IndividualSyncMapper ism = session.getMapper(IndividualSyncMapper.class);
        for(IndividualSync i : individualSyncAll){
            ism.addResultOneByOne(i);
        }
        session.commit();
        session.close();
    }

    @Override
    public List<String> findUserList(Long meetingID) {
        return individualSyncMapper.selectUserList(meetingID);
    }

    @Override
    public List<IndividualAllVO> findIndividualByUser(Long meetingID, String users) {
        return individualSyncMapper.selectIndividualByUser(meetingID,users);
    }
}
