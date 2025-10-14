package org.shortlinkbyself.pipo.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.google.protobuf.ServiceException;
import org.shortlinkbyself.pipo.project.dao.entity.ShortLinkDO;
import org.shortlinkbyself.pipo.project.dto.req.ShortLinkCreateReqDTO;
import org.shortlinkbyself.pipo.project.dto.req.ShortLinkPageReqDTO;
import org.shortlinkbyself.pipo.project.dto.req.ShortLinkUpdateReqDTO;
import org.shortlinkbyself.pipo.project.dto.resp.ShortLinkCreateRespDTO;
import org.shortlinkbyself.pipo.project.dto.resp.ShortLinkGroupCountQueryRespDTO;
import org.shortlinkbyself.pipo.project.dto.resp.ShortLinkPageRespDTO;

import java.util.List;

public interface ShortLinkService extends IService<ShortLinkDO> {
    ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO requestParam) throws ServiceException;

    IPage<ShortLinkPageRespDTO> pageShortlink(ShortLinkPageReqDTO requestParam);

    List<ShortLinkGroupCountQueryRespDTO> listGroupShortLinkCount(List<String> requestParam);

    Void updateShortLinkInfo(ShortLinkUpdateReqDTO shortLinkInfo);
}
