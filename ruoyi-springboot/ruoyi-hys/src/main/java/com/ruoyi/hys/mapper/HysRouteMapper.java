package com.ruoyi.hys.mapper;

import java.util.List;
import com.ruoyi.hys.domain.HysRoute;

/**
 * 旅游线路 数据层
 */
public interface HysRouteMapper {
    public HysRoute selectById(Long routeId);

    public List<HysRoute> selectOnline();

    public List<HysRoute> selectList(HysRoute route);

    public int insertRoute(HysRoute route);

    public int updateRoute(HysRoute route);

    public int deleteRouteByIds(Long[] routeIds);
}
