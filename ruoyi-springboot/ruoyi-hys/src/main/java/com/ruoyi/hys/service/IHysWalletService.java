package com.ruoyi.hys.service;

import java.util.List;
import java.util.Map;
import com.ruoyi.hys.domain.HysWalletTxn;

/**
 * 钱包服务（充值 / 互转 / 提现 / 流水）
 */
public interface IHysWalletService {

    /** 提交充值申请（单笔 ≤ 5000 元，审核通过后入账） */
    Map<String, Object> submitRecharge(Long memberId, long cents);

    /** 互转：会员之间一对一转账，双方余额原子更新 */
    Map<String, Object> transfer(Long memberId, String toPhone, long cents);

    /** 提现：6% 手续费按分向下取整，扣款即时、到账需人工审核 */
    Map<String, Object> withdraw(Long memberId, long cents);

    /** 我的流水 */
    List<HysWalletTxn> txns(Long memberId);

    /** 我的充值申请记录 */
    List<Map<String, Object>> myRecharges(Long memberId);

    /** 手机号脱敏 */
    String maskPhone(String phone);
}
