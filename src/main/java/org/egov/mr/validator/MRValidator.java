package org.egov.mr.validator;


import java.util.Arrays;
import java.util.List;

import javax.validation.Valid;

import org.apache.commons.lang.StringUtils;
import org.egov.mr.web.models.MarriageRegistration;
import org.egov.mr.web.models.MarriageRegistrationRequest;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Value;

import static org.egov.mr.util.MRConstants.businessService_MR;

public class MRValidator {
	
	@Value("${egov.allowed.businessServices}")
    private String allowedBusinessService;

	public void validateBusinessService(@Valid MarriageRegistrationRequest marriageRegistrationRequest,String businessServicefromPath) {
	

        List<String> allowedservices = Arrays.asList(allowedBusinessService.split(","));
        if (!allowedservices.contains(businessServicefromPath)) {
            throw new CustomException("BUSINESSSERVICE_NOTALLOWED", " The business service is not allowed in this module");
        }
        for (MarriageRegistration marriageRegistartions : marriageRegistrationRequest.getMarriageRegistrations()) {
            String licenseBusinessService = marriageRegistartions.getBusinessService()==null?businessService_MR:marriageRegistartions.getBusinessService();
            if (!StringUtils.equals(businessServicefromPath, licenseBusinessService)) {
                throw new CustomException("BUSINESSSERVICE_NOTMATCHING", " The business service inside license not matching with the one sent in path variable");
            }
        }
    
	
	
	}

}
