package org.shortlinkbyself.pipo.admin.controller;

import lombok.RequiredArgsConstructor;
import org.shortlinkbyself.pipo.admin.common.convention.result.Result;
import org.shortlinkbyself.pipo.admin.common.convention.result.Results;
import org.shortlinkbyself.pipo.admin.dto.resp.ShortLinkGroupRespDTO;
import org.shortlinkbyself.pipo.admin.service.GroupService;
import org.springframework.web.bind.annotation.*;

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
}
