package com..cms.tv.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.core.comn.contant.CommonConstant;
import com.core.comn.contant.Path;
import com.core.comn.exception.BaseException;
import com.core.comn.util.SecurityUtil;
import com.core.domain.CommonResultVO;
import com.core.domain.tv.DeepBrainResultVO;
import com.core.myService.common.CommonService;
import com.core.myService.tv.DeepBrainService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.Map;

@RestController
@Api(value = "딥브레인AI API 관리", description = "딥브레인AI API 관리")
public class DeepBrainController {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private DeepBrainService deepBrainService;

    @Autowired
    private TvService tvService;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    private CommonService commonService;


    @RequestMapping(value = "/sendtoproject", method = RequestMethod.GET)
    @ApiOperation(
            value = "프로젝트 내보내기 테스트 용도"
    )
    @ApiImplicitParams({
            @ApiImplicitParam(name = "tvId", value = "tvId", required = true, dataType = "string", paramType = "query"),
    })
    public CommonResultVO sendToDeepBrainProject(
            @RequestParam (defaultValue= "")  String tvId,
            @ApiIgnore @RequestParam Map<String, Object> params
    ) {
        CommonResultVO result = new CommonResultVO();
        try {
            result.setCode(CommonConstant.CommonCode.SUCCESS.getValue());
            TvDto tv = new TvDto();
            tv.setTvId(tvId);
            deepBrainService.updateAIInfo(tv);
        } catch (Exception e) {
            result.setCode(CommonConstant.CommonCode.FAIL.getValue());
        }
        return result;
    }

    @RequestMapping(value = "/{projectKey}", method = RequestMethod.GET)
    @ApiOperation(
            value = "프로젝트 가져오기(key) 테스트 용도"
    )
    public DeepBrainResultVO.DeepBrainResult sendToGetProject(
            @PathVariable("projectKey") String projectKey,
            @ApiIgnore @RequestParam Map<String, Object> params) {
        DeepBrainResultVO.DeepBrainResult result = new DeepBrainResultVO.DeepBrainResult();
        try {
            result.setCode(CommonConstant.CommonCode.SUCCESS.getValue());
            Map map = deepBrainService.sendToGetProject(projectKey);
            //deepBrainView.setView(deepBrainService.sendToGetProject(projectKey));
            result.setView(map);
        } catch (Exception e) {
            result.setCode(CommonConstant.CommonCode.FAIL.getValue());
        }
        return result;
    }

    @RequestMapping(value = Path.TV_MANAGEMENT_AISTUDIOS_CALLBACK, method = RequestMethod.POST)
    @ApiOperation(
            value = "프로젝트 CallBack 용도"
    )
    public void sendToCallBackProject(
            @ApiIgnore @RequestBody Map<String, Object> map) throws Exception {
        logger.info("===============================");
        logger.info("=====>"+ map);
        logger.info("===============================");
    }

    @RequestMapping(value = Path.TV_MANAGEMENT_AISTUDIOS_STATUS, method = RequestMethod.GET)
    @ApiOperation(
            value = "영상인코딩 진행율 확인",
            notes = "영상인코딩 진행율 확인"
    )
    @ApiImplicitParams({
            @ApiImplicitParam(name = "projectKey", value = "projectKey", required = true, dataType = "string",paramType = "query"),
            @ApiImplicitParam(name = "tvId", value = "tvId", required = true, dataType = "string", paramType = "query"),
    })
    public DeepBrainResultVO.DeepBrainResult getAistudiosStatus(@ApiIgnore @RequestParam Map<String, Object> params) {
        DeepBrainResultVO.DeepBrainResult result = new DeepBrainResultVO.DeepBrainResult();
        try {
            result.setCode(CommonConstant.CommonCode.SUCCESS.getValue());
            String projectKey = params.get("projectKey").toString();
            String tvId = params.get("tvId").toString();
            Map map = deepBrainService.sendToCheckProgress(projectKey);
            map.put("projectKey",projectKey);
            map.put("tvId",tvId);
            deepBrainService.aiProgress(map);
            result.setView(map);

        } catch (Exception e) {
            e.printStackTrace();
            result.setCode(CommonConstant.CommonCode.FAIL.getValue());
        }
        return result;
    }

    @RequestMapping(value = Path.TV_MANAGEMENT_AISTUDIOS_CONTROL, method = RequestMethod.POST)
    @ApiOperation(
            value = "스튜디오 이동",
            notes = "스튜디오 이동 저장 API 호출"
    )
    public DeepBrainResultVO.DeepBrainResult sendToCallProjectSave(
            @RequestBody TvDto tvDto
    ) {
        DeepBrainResultVO.DeepBrainResult result = new DeepBrainResultVO.DeepBrainResult();
        String userId = SecurityUtil.getUser();

        try {
            result.setCode(CommonConstant.CommonCode.SUCCESS.getValue());
            tvDto.setCreatedBy(userId);
            tvDto.setUpdatedBy(userId);
            tvDto = tvService.tvProc(tvDto, "N");
            Map map = deepBrainService.saveAIInfo(tvDto);
            result.setView(map);
        } catch (BaseException e) {
            result.setCode(CommonConstant.CommonCode.DUPL.getValue());
            result.setMessage(e.getMessage());
        } catch(Exception e) {
            result.setCode(CommonConstant.CommonCode.FAIL.getValue());
            result.setMessage(CommonConstant.ErrorCode.INSERT_FAIL.getValue());
        }

        return result;
    }

}
