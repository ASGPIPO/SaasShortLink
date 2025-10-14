package org.shortlinkbyself.pipo.project.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.google.protobuf.ServiceException;
import lombok.RequiredArgsConstructor;
import org.shortlinkbyself.pipo.project.common.convention.result.Result;
import org.shortlinkbyself.pipo.project.common.convention.result.Results;
import org.shortlinkbyself.pipo.project.dto.req.ShortLinkCreateReqDTO;
import org.shortlinkbyself.pipo.project.dto.req.ShortLinkPageReqDTO;
import org.shortlinkbyself.pipo.project.dto.req.ShortLinkUpdateReqDTO;
import org.shortlinkbyself.pipo.project.dto.resp.ShortLinkCreateRespDTO;
import org.shortlinkbyself.pipo.project.dto.resp.ShortLinkGroupCountQueryRespDTO;
import org.shortlinkbyself.pipo.project.dto.resp.ShortLinkPageRespDTO;
import org.shortlinkbyself.pipo.project.service.ShortLinkService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ShortLinkController {

    private final ShortLinkService shortLinkService;

    @PostMapping("/api/short-link/v1/create")
//    @SentinelResource(
//            value = "create_short-link",
//            blockHandler = "createShortLinkBlockHandlerMethod",
//            blockHandlerClass = CustomBlockHandler.class
//    )
    public Result<ShortLinkCreateRespDTO> createShortLink(@RequestBody ShortLinkCreateReqDTO requestParam) throws ServiceException {
        return Results.success(shortLinkService.createShortLink(requestParam));
    }

    @GetMapping("/api/short-link/v1/page")
    public Result<IPage<ShortLinkPageRespDTO>> pageShortLink(ShortLinkPageReqDTO requestParam) {
        return Results.success(shortLinkService.pageShortlink(requestParam));
    }

    @GetMapping("/api/short-link/v1/groupCount")
    public Result<List<ShortLinkGroupCountQueryRespDTO>> groupShortLinkCount(@RequestParam("gids") List<String> gids) {
        return Results.success(shortLinkService.listGroupShortLinkCount(gids));
    }

    @PutMapping("/api/short-link/v1/shortlink")
    public Result<Void> updateLinkInfo(@RequestBody ShortLinkUpdateReqDTO shortLinkInfo) {
        return Results.success(shortLinkService.updateShortLinkInfo(shortLinkInfo));
    }
}