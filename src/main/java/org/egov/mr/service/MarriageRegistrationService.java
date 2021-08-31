package org.egov.mr.service;

import static org.egov.mr.util.MRConstants.businessService_MR;

import java.util.List;

import javax.validation.Valid;

import org.egov.mr.validator.MRValidator;
import org.egov.mr.web.models.MarriageRegistration;
import org.egov.mr.web.models.MarriageRegistrationRequest;
import org.egov.tl.service.EnrichmentService;
import org.springframework.beans.factory.annotation.Autowired;




public class MarriageRegistrationService {
	
	
	
	private MRValidator mrValidator ;
	
	private EnrichmentService enrichmentService;

	@Autowired
	public MarriageRegistrationService(MRValidator mrValidator,org.egov.mr.service.EnrichmentService enrichmentService) {
		this.mrValidator = mrValidator;
		this.enrichmentService = enrichmentService;
	}

	public List<MarriageRegistration> create(@Valid MarriageRegistrationRequest marriageRegistrationRequest,String businessServicefromPath) {
		if(businessServicefromPath==null)
            businessServicefromPath = businessService_MR;
		
		mrValidator.validateBusinessService(marriageRegistrationRequest,businessServicefromPath);
		enrichmentService.enrichMRCreateRequest(marriageRegistrationRequest);
	       tlValidator.validateCreate(tradeLicenseRequest, mdmsData);
		
		return null;
	}

}
