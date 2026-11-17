package com.ruoyi.hys.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.hys.domain.HysMember;
import com.ruoyi.hys.mapper.HysMemberMapper;
import com.ruoyi.hys.service.IHysMemberService;

/**
 * 会员服务实现
 */
@Service
public class HysMemberServiceImpl implements IHysMemberService {

    private static final DateTimeFormatter D = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter T = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final HysMemberMapper memberMapper;

    public HysMemberServiceImpl(HysMemberMapper memberMapper) {
        this.memberMapper = memberMapper;
    }

    /* ------------------------------------------------------------ 注册登录 */

    @Override
    public Map<String, Object> register(Map<String, String> form) {
        String name = trim(form.get("name"));
        String phone = trim(form.get("phone"));
        String password = trim(form.get("password"));
        String referrerCode = trim(form.get("referrerCode"));
        Object regionObj = form.get("region");
        String region = regionObj == null ? "" : String.valueOf(regionObj).replace("[", "").replace("]", "").replace("\"", "").replace(",", "/");

        if (name == null || name.isEmpty()) {
            throw new ServiceException("请填写名称");
        }
        if (phone == null || !phone.matches("1\\d{10}")) {
            throw new ServiceException("手机号格式不正确");
        }
        if (memberMapper.selectByPhone(phone) != null) {
            throw new ServiceException("该手机号已注册");
        }
        if (password == null || password.length() < 6) {
            throw new ServiceException("密码至少 6 位");
        }
        if (region == null || region.split("/").length != 3 || region.endsWith("/")) {
            throw new ServiceException("请选择省 / 市 / 区（需选到区级）");
        }
        /* COMPLIANCE: 注册必须绑定推荐人（关系链不能断） */
        if (referrerCode == null || referrerCode.isEmpty()) {
            throw new ServiceException("推荐人码必填：请填写推荐人手机号");
        }
        HysMember referrer = memberMapper.selectByPhone(referrerCode);
        if (referrer == null) {
            throw new ServiceException("推荐人不存在，请核对手机号");
        }

        HysMember member = new HysMember();
        member.setName(name);
        member.setPhone(phone);
        member.setPassword(SecurityUtils.encryptPassword(password));
        member.setRegion(region);
        member.setReferrerId(referrer.getMemberId());
        member.setDirectIndex(memberMapper.countDirects(referrer.getMemberId()) + 1);
        member.setLevel("NORMAL");
        member.setVipExpireAt("");
        member.setBalanceCents(0L);
        member.setGreenPoints(0L);
        member.setUpgradedAt("");
        member.setCreateTime(LocalDateTime.now().format(T));
        memberMapper.insertMember(member);

        Map<String, Object> result = new HashMap<>();
        member.setPassword(null);
        result.put("member", member);
        result.put("referrerName", referrer.getName());
        result.put("token", null);
        return result;
    }

    @Override
    public Map<String, Object> login(String phone, String password) {
        HysMember member = memberMapper.selectByPhone(phone);
        if (member == null) {
            throw new ServiceException("账号不存在");
        }
        /* SEC: 密码字段缺失视为校验失败，绝不允许空密码绕过 */
        if (member.getPassword() == null || member.getPassword().isEmpty()
                || !SecurityUtils.matchesPassword(password, member.getPassword())) {
            throw new ServiceException("密码不正确");
        }
        member.setPassword(null);
        Map<String, Object> result = new HashMap<>();
        result.put("member", member);
        return result;
    }

    /* ------------------------------------------------------------ 资料 / 团队 */

    @Override
    public Map<String, Object> profile(Long memberId) {
        HysMember member = mustGet(memberId);
        member.setPassword(null);
        member.setVipState(vipState(member));
        HysMember referrer = member.getReferrerId() == null ? null : memberMapper.selectById(member.getReferrerId());
        Map<String, Object> profile = new HashMap<>();
        profile.put("member", member);
        profile.put("referrerName", referrer == null ? "—" : referrer.getName());
        return profile;
    }

    @Override
    public Map<String, Object> teamStats(Long memberId) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("team", memberMapper.countDescendants(memberId));
        stats.put("direct", memberMapper.countDirects(memberId));
        return stats;
    }

    @Override
    public List<HysMember> directList(Long memberId) {
        List<HysMember> directs = memberMapper.selectDirects(memberId);
        for (HysMember d : directs) {
            d.setPassword(null);
            d.setTeamCount(memberMapper.countDescendants(d.getMemberId()));
            d.setDirectCount(memberMapper.countDirects(d.getMemberId()));
            HysMember up = d.getReferrerId() == null ? null : memberMapper.selectById(d.getReferrerId());
            d.setReferrerName(up == null ? "—" : up.getName());
            d.setVipState(vipState(d));
        }
        return directs;
    }

    /* ------------------------------------------------------------ 密码 */

    @Override
    public void changePassword(Long memberId, String oldPwd, String newPwd) {
        HysMember member = mustGet(memberId);
        /* SEC: 存量密码缺失同样拒绝，不允许空密码绕过原密码校验 */
        if (member.getPassword() == null || member.getPassword().isEmpty()
                || !SecurityUtils.matchesPassword(oldPwd, member.getPassword())) {
            throw new ServiceException("原密码不正确");
        }
        if (newPwd == null || newPwd.length() < 6) {
            throw new ServiceException("新密码至少 6 位");
        }
        HysMember update = new HysMember();
        update.setMemberId(memberId);
        update.setPassword(SecurityUtils.encryptPassword(newPwd));
        memberMapper.updateMember(update);
    }

    @Override
    public void resetPassword(String phone, String newPwd) {
        HysMember member = memberMapper.selectByPhone(phone);
        if (member == null) {
            throw new ServiceException("账号不存在");
        }
        if (newPwd == null || newPwd.length() < 6) {
            throw new ServiceException("新密码至少 6 位");
        }
        HysMember update = new HysMember();
        update.setMemberId(member.getMemberId());
        update.setPassword(SecurityUtils.encryptPassword(newPwd));
        memberMapper.updateMember(update);
    }

    /* ------------------------------------------------------------ VIP 状态 */

    @Override
    public boolean isVipActive(HysMember member) {
        if (member == null || !"VIP".equals(member.getLevel())) {
            return false;
        }
        if (member.getVipExpireAt() == null || member.getVipExpireAt().isEmpty()) {
            return true;
        }
        LocalDate expire = LocalDate.parse(member.getVipExpireAt(), D);
        return !expire.isBefore(LocalDate.now());
    }

    @Override
    public String vipState(HysMember member) {
        if (member == null || !"VIP".equals(member.getLevel()) || member.getVipExpireAt() == null || member.getVipExpireAt().isEmpty()) {
            return "none";
        }
        long left = ChronoUnit.DAYS.between(LocalDate.now(), LocalDate.parse(member.getVipExpireAt(), D));
        if (left < 0) {
            return "expired";
        }
        if (left <= 30) {
            return "soon";
        }
        return "active";
    }

    @Override
    public long priceFor(long normalCents, long vipCents, HysMember member) {
        return isVipActive(member) ? vipCents : normalCents;
    }

    /* ------------------------------------------------------------ 工具 */

    public HysMember mustGet(Long memberId) {
        HysMember member = memberMapper.selectById(memberId);
        if (member == null) {
            throw new ServiceException("会员不存在");
        }
        return member;
    }

    private String trim(String s) {
        return s == null ? null : s.trim();
    }
}
