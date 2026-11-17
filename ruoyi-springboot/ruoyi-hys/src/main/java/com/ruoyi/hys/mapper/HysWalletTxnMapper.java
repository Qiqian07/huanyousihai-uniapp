package com.ruoyi.hys.mapper;

import java.util.List;
import com.ruoyi.hys.domain.HysWalletTxn;

/**
 * 余额流水 数据层
 */
public interface HysWalletTxnMapper {
    public List<HysWalletTxn> selectByMember(Long memberId);

    public List<HysWalletTxn> selectList(HysWalletTxn txn);

    public int insertTxn(HysWalletTxn txn);

    public int updateStatus(Long txnId, String status);
}
