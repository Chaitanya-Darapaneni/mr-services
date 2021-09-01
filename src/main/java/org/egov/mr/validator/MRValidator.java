package org.egov.mr.validator;


import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

	public void validateCreate(MarriageRegistrationRequest marriageRegistrationRequest) {
        List<MarriageRegistration> marriageRegistrations = marriageRegistrationRequest.getMarriageRegistrations();
        String businessService = marriageRegistrationRequest.getMarriageRegistrations().isEmpty()?null:request.getMarriageRegistrations().get(0).getBusinessService();
        
        if (businessService == null)
            businessService = businessService_MR;
        switch (businessService) {
            case businessService_MR:
                validateMRSpecificNotNullFields(marriageRegistrationRequest);
                break;

        }
        mdmsValidator.validateMdmsData(request, mdmsData);
        validateInstitution(request);
        validateDuplicateDocuments(request);
    }

	private void validateMRSpecificNotNullFields(MarriageRegistrationRequest marriageRegistrationRequest) {
		
		marriageRegistrationRequest.getMarriageRegistrations().forEach(marriageRegistration -> {
			
			 Map<String, String> errorMap = new HashMap<>();
			
			if(marriageRegistration.getMarriageDate() == null)
				errorMap.put("NULL_MARRIAGEDATE", " Marriage Date cannot be null");
			
			if(marriageRegistration.getMarriagePlace().getWard() == null)
				errorMap.put("NULL_WARD", " Ward cannot be null");
		
			if(marriageRegistration.getMarriagePlace().getPlaceOfMarriage() == null)
				errorMap.put("NULL_MARRIAGEPLACE", " Marriage Place  cannot be null");
			
			if(marriageRegistration.getMarriagePlace().getLocality().getCode() == null)	
				errorMap.put("NULL_LOCALITY", " Locality  cannot be null");

			if(marriageRegistration.getTenantId() == null)
				errorMap.put("NULL_TENANTID", " Tenant id cannot be null");
			
			if(marriageRegistration.getCoupleDetails()==null)
				errorMap.put("COUPLE_DETAILS_ERROR", " Couple Details are mandatory  ");
			
			if(marriageRegistration.getCoupleDetails().size()!=2)
				errorMap.put("COUPLE_DETAILS_ERROR", " Both the Bride and Groom details should be provided .");
			
			
			
			
			
			if (!errorMap.isEmpty())
                throw new CustomException(errorMap);
			
		});
		
	}

}
