package com.ruoyi.hys.mapper;

import java.util.List;
import com.ruoyi.hys.domain.HysAlbum;

/**
 * 相册批次 数据层
 */
public interface HysAlbumMapper {
    public HysAlbum selectById(Long albumId);

    public List<HysAlbum> selectList(HysAlbum album);

    public int insertAlbum(HysAlbum album);

    public int updateAlbum(HysAlbum album);

    public int deleteAlbumByIds(Long[] albumIds);
}
