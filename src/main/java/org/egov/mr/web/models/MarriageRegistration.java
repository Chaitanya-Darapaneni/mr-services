package org.egov.mr.web.models;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;


import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MarriageRegistration {
	
	@Size(max=64)
    @JsonProperty("id")
    private String id = null;

    @NotNull
    @Size(max=64)
    @JsonProperty("tenantId")
    private String tenantId = null;
    
    @Size(max=64)
    @JsonProperty("accountId")
    private String accountId = null;
    
    @JsonProperty("businessService")
    private String businessService = "MR";
    
    @JsonProperty("workflowCode")
    private String workflowCode = null;
    
    @Size(max=64)
    @JsonProperty("mrNumber")
    private String mrNumber = null;

    @Size(max=64)
    @JsonProperty("applicationNumber")
    private String applicationNumber;
    
    @Size(max=64)
    @JsonProperty("oldReferenceId")
    private String oldReferenceId;
    
    @Size(max=64)
    @JsonProperty("oldApplicationNumber")
    private String oldApplicationNumber;
    
    
    @JsonProperty("applicationDate")
    private Long applicationDate = null;

    @JsonProperty("marriageDate")
    private Long marriageDate = null;
    
    @JsonProperty("issuedDate")
    private Long issuedDate = null;
    
    @NotNull
    @Size(max=64)
    @JsonProperty("action")
    private String action = null;

    @Size(max=64)
    @JsonProperty("status")
    private String status = null;


    @JsonProperty("auditDetails")
    private AuditDetails auditDetails = null;
    

    @Valid
    @NotNull
    @JsonProperty("marriageRegistrationDetail")
    private MarriageRegistrationDetail marriageRegistrationDetail = null;
    
}
