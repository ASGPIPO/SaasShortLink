package org.shortlinkbyself.pipo.admin.controller;

import lombok.RequiredArgsConstructor;
import org.shortlinkbyself.pipo.admin.common.convention.result.Result;
import org.shortlinkbyself.pipo.admin.common.convention.result.Results;
import org.shortlinkbyself.pipo.admin.dto.req.ShortLinkGroupSortReqDTO;
import org.shortlinkbyself.pipo.admin.dto.resp.ShortLinkGroupRespDTO;
import org.shortlinkbyself.pipo.admin.service.GroupService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/short-link/admin/v1")
@RequiredArgsConstructor
public class GroupController {
    private final GroupService groupService;

    @PostMapping("/group")
    public Result<Void> save(@RequestBody ShortLinkGroupRespDTO requestParam) {
        groupService.saveGroup(requestParam.getName());
        return Results.success();

    }

    @GetMapping("/group")
    public Result<List<ShortLinkGroupRespDTO>> getGroupList() {
        return Results.success(groupService.getGroupList());
    }

    @PutMapping("/group")
    public Result<Void> updateGroup(@RequestBody ShortLinkGroupRespDTO requestParam) {
        groupService.updateGroup(requestParam);
        return Results.success();

    }
    @DeleteMapping("/group")
    public Result<Void> updateGroup(@RequestParam String gid) {
        groupService.deleteGroup(gid);
        return Results.success();

    }

    /**
     * 排序短链接分组
     */
    @PostMapping("/group/sort")
    public Result<Void> sortGroup(@RequestBody List<ShortLinkGroupSortReqDTO> requestParam) {
        groupService.sortGroup(requestParam);
        return Results.success();
    }

}
