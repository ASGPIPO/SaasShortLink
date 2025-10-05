package org.shortlinkbyself.pipo.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.shortlinkbyself.pipo.admin.dao.entity.GroupDO;
import org.shortlinkbyself.pipo.admin.dto.req.ShortLinkGroupSortReqDTO;
import org.shortlinkbyself.pipo.admin.dto.resp.ShortLinkGroupRespDTO;

import java.util.List;

public interface GroupService extends IService<GroupDO> {


    void saveGroup(String groupName);

    void saveGroup(String username, String groupName);

    List<ShortLinkGroupRespDTO> getGroupList();

    void updateGroup(ShortLinkGroupRespDTO requestParam);

    void deleteGroup(String gid);

    void sortGroup(List<ShortLinkGroupSortReqDTO> requestParam);
}
