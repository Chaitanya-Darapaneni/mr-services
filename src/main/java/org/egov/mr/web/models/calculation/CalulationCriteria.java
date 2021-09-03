package org.egov.mr.web.models.calculation;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.egov.mr.web.models.MarriageRegistration;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Either marriageRegistration object or the application number is mandatory apart from tenantid.
 */

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CalulationCriteria {
        @JsonProperty("marriageRegistration")
        @Valid
private MarriageRegistration marriageRegistration = null;

        @JsonProperty("applicationNumber")
        @Size(min=2,max=64) 
private String applicationNumber = null;

        @JsonProperty("tenantId")
        @NotNull@Size(min=2,max=256) 
private String tenantId = null;


}

