package com.core.myService.tv;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.core.comn.contant.CommonConstant;
import com.core.comn.util.RestApiUtil;
import com.core.comn.util.SecurityUtil;
import com.core.myMapper.tv.TtvAutoMapper;
import com.core.myService.common.CommonService;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class DeepBrainService {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Value("${deepBrain.host}")
    private String host;

    @Value("${deepBrain.accessToken}")
    private String accessToken;

    @Value("${deepBrain.ssoKey}")
    private String ssoKey;

    @Value("${deepBrain.callbackUrl}")
    private String callbackUrl;

    private static final String logoImage = "https://image-1.png";

    private static final String labelGeneralImage = "https://image-general-2.png";

    private static final String labelSpeedImage = "https://image-speed.png";
    
    private static final String coverImage = "https://cover_v3.png";

    @Autowired
    private RestApiUtil restApiUtil;

    @Autowired
    private TvService tvService;

    @Autowired
    private TvVodService tvVodService;

    @Autowired
    private TvSlideService tvSlideService;

    @Autowired
    private TvThumbnailService tvThumbnailService;

    @Autowired
    private CommonService commonService;

    @Autowired
    private TvAutoService tvAutoService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TvAutoMapper tvAutoMapper;

    
    /*-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
    | CMS service
    |-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/

    /**
     * ???????????? ????????????
     *
     * @return
     * @throws Exception
     *  */
    /* ex)
     *  {
     *      "success": true,
     *      "data": {
     *          "key": "?????????",
     *          "cubes_used": 1.5
     *      }
     *  }
    */
    public Map sendToDeepBrainProject(JSONObject deepBrainData) throws Exception {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Authorization", accessToken);
        Map result = (Map) restApiUtil.excute(host + "/project", HttpMethod.POST, deepBrainData, Map.class, RestApiUtil.MEDIA_TYPE_JSON, httpHeaders);

        return result;
    }

    /**
     * ???????????? ?????? ????????? ?????? ?????? API ??????
     * @param deepBrainData
     * @return
     * @throws Exception
     */
    public Map sendToDeepBrainSave(JSONObject deepBrainData) throws Exception {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Authorization", "?????????");
        Map result = (Map) restApiUtil.excute( host + "/project/save", HttpMethod.POST, deepBrainData, Map.class, RestApiUtil.MEDIA_TYPE_JSON, httpHeaders);

        return result;
    }


    /**
     * ???????????? ????????? ??????
     *
     * @param projectId
     * @return
     * @throws Exception
     */
    /*
    {
        "success": true,
            "data": {
                "progress": 100,
                "url": "https://resource.deepbrainai.io/exports/output_.mp4"
            }
    }
    */
    public Map sendToCheckProgress(String projectId) throws Exception {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Authorization", "???????????????");
        Map result = (Map) restApiUtil.excute(host + "/progress/" + projectId, HttpMethod.GET, null, Map.class, MediaType.APPLICATION_JSON_UTF8_VALUE, httpHeaders);
        return result;
    }

   
    public Map sendToGetProject(String projectId) throws Exception {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Authorization", accessToken);
        Map result = (Map) restApiUtil.excute(host + "/project/" + projectId, HttpMethod.GET, null, Map.class, MediaType.APPLICATION_JSON_UTF8_VALUE, httpHeaders);
        return result;
    }

    

    /**
     * image/background ??????
     * ???????????????/???????????????/???????????????/???????????????
     * @return
     */
    private JSONArray makeClips(TtvSlideDto slideDto,String typeCode) {
        JSONArray clip = new JSONArray();

        
        JSONObject logoObject = new JSONObject();
        
        //???????????????
        logoObject.put("type", "image");
        logoObject.put("detail", makeClipImageDetail(slideDto,"logo"));
        clip.add(logoObject);
        
        //???????????????
        if(StringUtils.equals(CommonConstant.ArticleType.Speed.getKey(),typeCode)
                || StringUtils.equals(CommonConstant.ArticleType.Exclusive.getKey(),typeCode)) {
            JSONObject labelObject = new JSONObject();
            labelObject.put("type", "image");
            labelObject.put("detail", makeClipLabelImageDetail(typeCode));
            clip.add(labelObject);
        }

        //??????B?????? ?????????????????????
        if(StringUtils.equals(CommonConstant.TtvAiVodMode.VM007.getKey(),slideDto.getTtvSlidesVodMode())){
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("type", "image");
            jsonObject.put("detail", makeClipImageDetail(slideDto,"cover"));
            clip.add(jsonObject);
        }
        //?????????????????? ?????? ??????
        if(StringUtils.isNotBlank(slideDto.getTtvSlidesDataFile())){
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("type", "image");
            jsonObject.put("detail", makeClipImageDetail(slideDto,"image"));
            clip.add(jsonObject);
        }

        //?????????????????? ?????? ??????
        if(StringUtils.isNotBlank(slideDto.getTtvSlidesBackgroundFile())){
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("type", "background");
            jsonObject.put("detail", makeClipBackGroundDetail(slideDto.getTtvSlidesBackgroundFile()));
            clip.add(jsonObject);
        }

        return clip;
    }

    /**
     * ??????????????? ??????
     * @param typeCode
     * @return
     */
    private JSONObject makeClipLabelImageDetail(String typeCode) {

        JSONObject jsonObject = new JSONObject();

        jsonObject.put("locationX", -0.42);
        jsonObject.put("locationY", -0.41);
        jsonObject.put("scale",  0.2);
        jsonObject.put("layer", 516);
        if(StringUtils.equals(CommonConstant.ArticleType.Speed.getKey(),typeCode)) {
            jsonObject.put("url", labelSpeedImage);
        }else {
            jsonObject.put("url", labelGeneralImage);
        }
        return jsonObject;
    }

    /**
     * ???????????????/??????????????? ??????
     * @param slideDto
     * @param mode
     * @return
     */
    private JSONObject makeClipImageDetail(TtvSlideDto slideDto,String mode) {
        Map<String, Object> properties = null;
        if(StringUtils.equals("logo",mode)){
            properties = new HashMap<>();
            properties.put("x", 0.42);
            properties.put("y", -0.42);
            properties.put("s",0.2);
            properties.put("url",logoImage);
            properties.put("layer",515);
        }else if(StringUtils.equals("cover",mode)){
            properties = new HashMap<>();
            properties.put("x",0);
            properties.put("y",0);
            properties.put("s",2.1);
            properties.put("url",coverImage);
            properties.put("layer",512);
        }else {
            properties = setPropertiesbyClipMode(slideDto);
        }

        JSONObject jsonObject = new JSONObject();

        jsonObject.put("locationX", properties.get("x"));
        jsonObject.put("locationY", properties.get("y"));
        jsonObject.put("scale",  properties.get("s"));
        jsonObject.put("layer", properties.get("layer"));
        jsonObject.put("url", properties.get("url"));

        return jsonObject;
    }

    /**
     * ?????? ??????
     * @param slideDto
     * @return
     */
    private Map<String, Object> setPropertiesbyClipMode(TvSlideDto slideDto){
        Map<String, Object> properties = new HashMap<>();
        properties.put("url",slideDto.getTvDataFile());
        properties.put("layer",511);
        properties.put("x",0);
        properties.put("y",0);

        //??????A
        if(StringUtils.equals(CommonConstant.TvAiVodMode.003.getKey(),slideDto.getTvVodMode())){
            properties.put("s",1.5);
            //??????B
        }else if(StringUtils.equals(CommonConstant.TvAiVodMode.004.getKey(),slideDto.getTvVodMode())){
            //???????????????: CDN URL, X:0, Y:0, Scale: 250%(2.5)
            properties.put("s",2.5);
            //??????C
        }else if(StringUtils.equals(CommonConstant.TvAiVodMode.005.getKey(),slideDto.getTvVodMode())){
            //???????????????: CDN URL, X:0, Y:0, Scale: 250%(2.5)
            properties.put("s",2.5);
            //??????A
        }else if(StringUtils.equals(CommonConstant.TvAiVodMode.008.getKey(),slideDto.getTvVodMode()) ||
                StringUtils.equals(CommonConstant.TvAiVodMode.007.getKey(),slideDto.getTvVodMode())
        ){
            //???????????????: CDN URL, X:0, Y:0, Scale: 150%(1.5)
            //???????????????: CDN URL, X:0, Y:0, Scale: 150%(1.5)
            properties.put("s",2.5);
        } else if(StringUtils.equals(CommonConstant.TvAiVodMode.006.getKey(),slideDto.getTvVodMode())){
            //???????????????: CDN URL, X:0, Y:0, Scale: 250%(2.5)
            properties.put("s",1.5);
        }

        return properties;

    }

    /**
     * ??????????????? ????????? ??????
     * @param fileUrl
     * @return
     */
    private JSONObject makeClipBackGroundDetail(String fileUrl) {
        //tpye background ?????? url???
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("url", fileUrl);
       // jsonObject.put("url", "https://cdn.aistudios.com/images/news/aiplatform_background_space.png");
        return jsonObject;
    }




    /**
     * AI ?????? ??????
     *
     * @return
     */
    private JSONObject makeAIModel(TtvSlideDto slideDto) {
        JSONObject jsonObject = new JSONObject();

        Map<String, Object> properties = setPropertiesbyHumanMode(slideDto);
        //????????? ?????? location + Scale ?????? ??????
        //?????? ?????? ????????? ??????
        jsonObject.put("model", slideDto.getTvHumanType());
        //jsonObject.put("model", "hankook_choiseoeun");
        jsonObject.put("locationX", properties.get("x"));
        jsonObject.put("locationY", properties.get("y"));
        jsonObject.put("layer", 520);
        jsonObject.put("clothes", slideDto.getTvHumanDress());
        jsonObject.put("scale", properties.get("s"));
        jsonObject.put("language", "ko");
        jsonObject.put("script", slideDto.getTvContent());

        return jsonObject;
    }

    /**
     * AI ?????? ??????
     * ??????A/??????B/??????A/??????B/??????C/??????A/??????B/??????C
     * @return
     */
    private Map<String, Object> setPropertiesbyHumanMode(TtvSlideDto slideDto){
        Map<String, Object> properties = new HashMap<>();
        //??????A
        if(StringUtils.equals(CommonConstant.TtvAiVodMode.001.getKey(),slideDto.getTtvSlidesVodMode())){
            /*properties.put("x",0);
            properties.put("y",0.2);
            properties.put("s",0.75);*/
            properties.put("x",0);
            properties.put("y",0.52);
            properties.put("s",1.25);
        //??????B
        }else if(StringUtils.equals(CommonConstant.TtvAiVodMode.002.getKey(),slideDto.getTtvSlidesVodMode())){
            /*properties.put("x",-0.3);
            properties.put("y",0.2);
            properties.put("s",0.7);*/
            properties.put("x",-0.22);
            properties.put("y",0.52);
            properties.put("s",1.15);
        //??????A
        }else if(StringUtils.equals(CommonConstant.TtvAiVodMode.003.getKey(),slideDto.getTtvSlidesVodMode())){
            /*properties.put("x",-0.3);
            properties.put("y",0.25);
            properties.put("s",0.55);*/
            properties.put("x",-0.28);
            properties.put("y",0.5);
            properties.put("s",0.9);
        //??????B
        }else if(StringUtils.equals(CommonConstant.TtvAiVodMode.004.getKey(),slideDto.getTtvSlidesVodMode())){
            /*properties.put("x",-0.3);
            properties.put("y",0.25);
            properties.put("s",0.55);*/
            properties.put("x",-0.28);
            properties.put("y",0.5);
            properties.put("s",0.9);
        //??????C
        }else if(StringUtils.equals(CommonConstant.TtvAiVodMode.005.getKey(),slideDto.getTtvSlidesVodMode())){
            /*properties.put("x",-0.3);
            properties.put("y",0.25);
            properties.put("s",0.7);*/
            properties.put("x",-0.25);
            properties.put("y",0.5);
            properties.put("s",1.05);
        //??????A
        }else if(StringUtils.equals(CommonConstant.TtvAiVodMode.006.getKey(),slideDto.getTtvSlidesVodMode()) ||
                StringUtils.equals(CommonConstant.TtvAiVodMode.007.getKey(),slideDto.getTtvSlidesVodMode()) ||
        StringUtils.equals(CommonConstant.TtvAiVodMode.008.getKey(),slideDto.getTtvSlidesVodMode())
        ){
            properties.put("x",-30);
            properties.put("y",20);
            properties.put("s",0.75);
        }

        return properties;

    }

    /**
     *  ?????? ????????? ?????? ??? ?????? ????????? ????????????
     * @param map
     * @throws Exception
     */
    public void aiProgress(Map map) throws Exception {
        Map data = objectMapper.convertValue(map.get("data"), Map.class);
        String ttvId = map.get("ttvId").toString();
        String projectKey = map.get("projectKey").toString();
        
        //??????????????? ???????????? ??????
        if (StringUtils.equals(map.get("success").toString(), "true"))
            if(data.get("progress") instanceof Integer && (int)data.get("progress") >= 100){
                Map projectMap = sendToGetProject(projectKey);
                Map response = objectMapper.convertValue(projectMap.get("response"), Map.class);

                TtvDto ttv = getTtvDto(ttvId , response);
                ttv.setTtvAistudioJson("JSON:"+String.valueOf(data) +"/ response:"+ response);
                TtvThumbnailDto ttvThumbnail = getTtvThumbnailDto(ttvId,response);

                TtvVodDto ttvVod = getTtvVodDto(ttvId,response);

                TtvActHisDto ttvActHisDto = getTtvHisDto(ttv,CommonConstant.TtvActHisCode.H005.getKey());

                updateAIInfo(ttv, ttvThumbnail, ttvVod,ttvActHisDto);
            }else{
                //????????? ?????? 100????????? ???????????? ???????????? 10 ??? ???????????? ????????????
                int ttv_his =  ttvService.getTtvHis(ttvId);
                if(ttv_his > 10){
                    TtvDto ttv = getTtvDtoFail(ttvId);
                    ttv.setTtvAistudioJson("JSON:"+String.valueOf(data));

                    TtvThumbnailDto ttvThumbnail = getTtvThumbnailFailDto(ttvId);

                    TtvVodDto ttvVod = getTtvVodFailDto(ttvId);

                    TtvActHisDto ttvActHisDto = getTtvHisDto(ttv,CommonConstant.TtvActHisCode.H006.getKey());
                    updateAIInfo(ttv, ttvThumbnail, ttvVod,ttvActHisDto);
                }else{
                    //????????? ?????? 100?????? ?????? 10 ??? ???????????? ?????? ??????
                    Map projectMap = sendToGetProject(projectKey);
                    Map response = objectMapper.convertValue(projectMap.get("response"), Map.class);
                    TtvDto ttv = ttvService.getTtv(ttvId);
                    ttv.setTtvHistoryId(commonService.getUUID());
                    ttv.setTtvAistudioJson("JSON:"+String.valueOf(data) +"/ response:"+ response);
                    if(!ObjectUtils.isEmpty(data)){
                        if(data.get("progress") instanceof Integer){
                            ttv.setTtvProgress((int)data.get("progress"));
                        }else{
                            ttv.setTtvProgress((double)data.get("progress"));
                        }
                    }
                    updateAIInfoIng(ttv);
                }
            }
    }
    
    private TtvActHisDto getTtvHisDto(TtvDto ttv,String actHisType) throws Exception {
        TtvActHisDto ttvActHisDto = new TtvActHisDto();
        ttvActHisDto.setTtvActHisId(commonService.getUUID());
        ttvActHisDto.setTtvId(ttv.getTtvId());
        ttvActHisDto.setTtvActHisType(actHisType);
        ttvActHisDto.setTtvHistoryId(ttv.getTtvHistoryId());
        ttvActHisDto.setCreatedBy(SecurityUtil.getUser());
        return ttvActHisDto;
    }

    private TtvDto getTtvDto(String ttvId, Map response) throws Exception {
        TtvDto ttv = ttvService.getTtv(ttvId);
        ttv.setTtvVodFile(response.get("videoUrl").toString());
        ttv.setTtvStatus(CommonConstant.TtvStatusCode.S003.getKey());
        ttv.setTtvThumbnailFile(response.get("thumbnailUrl").toString());
        ttv.setTtvHistoryId(commonService.getUUID());
        ttv.setTtvEditingStudio("N");
        ttv.setCreatedBy(SecurityUtil.getUser());
        ttv.setUpdatedBy(SecurityUtil.getUser());
        if(response.get("duration") instanceof Integer){
            int duration = (int) response.get("duration");
            ttv.setTtvVodPlaytime(Double.valueOf(duration));
        }else{
            ttv.setTtvVodPlaytime((double) response.get("duration"));
        }
        return ttv;
    }

    private TtvDto getTtvDtoFail(String ttvId) throws Exception {
        TtvDto ttv = ttvService.getTtv(ttvId);
        ttv.setTtvStatus(CommonConstant.TtvStatusCode.S005.getKey());
        ttv.setTtvHistoryId(commonService.getUUID());
        return ttv;
    }


    private TtvVodDto getTtvVodDto(String ttvId, Map response) throws Exception {
        TtvVodDto vod  = ttvVodService.getTtvVod(ttvId);
        if(ObjectUtils.isEmpty(vod))
        {
            TtvVodDto ttvVod = new TtvVodDto();
            ttvVod.setTtvVodId(commonService.getUUID());
            ttvVod.setTtvVodHistoryId(commonService.getUUID());
            ttvVod.setTtvId(ttvId);
            ttvVod.setTtvVodFile(response.get("videoUrl").toString());
            ttvVod.setTtvVodStatus(CommonConstant.TtvStatusCode.S003.getKey());
            ttvVod.setCreatedBy(SecurityUtil.getUser());
            if(response.get("duration") instanceof Integer){
                int duration = (int) response.get("duration");
                ttvVod.setTtvVodPlayTime(Double.valueOf(duration));
            }else{
                ttvVod.setTtvVodPlayTime((double) response.get("duration"));
            }
            return ttvVod;
        }
        vod.setTtvId(ttvId);
        vod.setTtvVodHistoryId(commonService.getUUID());
        vod.setTtvVodFile(response.get("videoUrl").toString());
        vod.setTtvVodStatus(CommonConstant.TtvStatusCode.S003.getKey());
        vod.setCreatedBy(SecurityUtil.getUser());
        if(response.get("duration") instanceof Integer){
            int duration = (int) response.get("duration");
            vod.setTtvVodPlayTime(Double.valueOf(duration));
        }else{
            vod.setTtvVodPlayTime((double) response.get("duration"));
        }

        return vod;
    }

    private TtvThumbnailDto getTtvThumbnailDto(String ttvId,Map response) throws Exception {
        TtvThumbnailDto thumbnail = ttvThumbnailService.getTtvThumbnail(ttvId);
        if(ObjectUtils.isEmpty(thumbnail))
        {
            TtvThumbnailDto ttvThumbnailDto = new TtvThumbnailDto();
            ttvThumbnailDto.setTtvThumbnailHistoryId(commonService.getUUID());
            ttvThumbnailDto.setTtvThumbnailId(commonService.getUUID());
            ttvThumbnailDto.setTtvId(ttvId);
            ttvThumbnailDto.setTtvThumbnailFile(response.get("thumbnailUrl").toString());
            ttvThumbnailDto.setCreatedBy(SecurityUtil.getUser());
            return ttvThumbnailDto;
        }
            thumbnail.setTtvThumbnailHistoryId(commonService.getUUID());
            thumbnail.setTtvId(ttvId);
            thumbnail.setTtvThumbnailFile(response.get("thumbnailUrl").toString());
            thumbnail.setCreatedBy(SecurityUtil.getUser());
        return thumbnail;
    }
    private TtvVodDto getTtvVodFailDto(String ttvId) throws Exception {
        TtvVodDto vod  = ttvVodService.getTtvVod(ttvId);
        if(ObjectUtils.isEmpty(vod))
        {
            TtvVodDto ttvVod = new TtvVodDto();
            ttvVod.setTtvVodId(commonService.getUUID());
            ttvVod.setTtvVodHistoryId(commonService.getUUID());
            ttvVod.setTtvId(ttvId);
            ttvVod.setTtvVodStatus(CommonConstant.TtvStatusCode.S005.getKey());
            ttvVod.setCreatedBy(SecurityUtil.getUser());
            return ttvVod;
        }
        vod.setTtvId(ttvId);
        vod.setTtvVodHistoryId(commonService.getUUID());
        vod.setTtvVodStatus(CommonConstant.TtvStatusCode.S005.getKey());
        vod.setCreatedBy(SecurityUtil.getUser());

        return vod;
    }
    private TtvThumbnailDto getTtvThumbnailFailDto(String ttvId) throws Exception {
        TtvThumbnailDto thumbnail = ttvThumbnailService.getTtvThumbnail(ttvId);
        if(ObjectUtils.isEmpty(thumbnail))
        {
            TtvThumbnailDto ttvThumbnailDto = new TtvThumbnailDto();
            ttvThumbnailDto.setTtvThumbnailHistoryId(commonService.getUUID());
            ttvThumbnailDto.setTtvThumbnailId(commonService.getUUID());
            ttvThumbnailDto.setTtvId(ttvId);
            ttvThumbnailDto.setCreatedBy(SecurityUtil.getUser());
            return ttvThumbnailDto;
        }
        thumbnail.setTtvThumbnailHistoryId(commonService.getUUID());
        thumbnail.setTtvId(ttvId);
        thumbnail.setCreatedBy(SecurityUtil.getUser());
        return thumbnail;
    }


    /*-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
   | API service
   |-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=*/

}
