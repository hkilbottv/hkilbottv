package com.core.domain.tv;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.core.domain.CommonResultVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

public class DeepBrainResultVO {

    @Data
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.ALWAYS)
    @NoArgsConstructor
    @ApiModel(description = "DeepBrain 상세조회")
    public static class DeepBrainResult extends CommonResultVO {
        @ApiModelProperty(notes = "DeepBrain 데이터")
        private Map view;
    }

}
