package org.shortlinkbyself.pipo.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.shortlinkbyself.pipo.admin.dao.entity.GroupDO;

public interface GroupService extends IService<GroupDO> {


    void saveGroup(String groupName);

    void saveGroup(String username, String groupName);
}
