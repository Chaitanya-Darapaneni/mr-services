package org.egov.mr.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.egov.common.contract.request.RequestInfo;
import org.egov.mr.repository.ServiceRequestRepository;
import org.egov.mr.util.MarriageRegistrationUtil;
import org.egov.mr.web.models.MarriageRegistration;
import org.egov.mr.web.models.MarriageRegistrationRequest;
import org.egov.mr.web.models.calculation.Calculation;
import org.egov.mr.web.models.calculation.CalculationRes;
import org.egov.mr.web.models.calculation.Category;
import org.egov.mr.web.models.calculation.FeeAndBillingSlabIds;
import org.egov.mr.web.models.calculation.TaxHeadEstimate;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.databind.ObjectMapper;


@Service
public class CalculationService {

    private MarriageRegistrationUtil utils;

    private ServiceRequestRepository serviceRequestRepository;

    private ObjectMapper mapper;


    @Autowired
    public CalculationService(MarriageRegistrationUtil utils, ServiceRequestRepository serviceRequestRepository, ObjectMapper mapper) {
        this.utils = utils;
        this.serviceRequestRepository = serviceRequestRepository;
        this.mapper = mapper;
    }


    /**
     * Adds the calculation object to the request
     * @param request The input create or update request
     * @return request with calculation object added
     */
    public List<MarriageRegistration> addCalculation(MarriageRegistrationRequest request){
        RequestInfo requestInfo = request.getRequestInfo();
        List<MarriageRegistration> licenses = request.getMarriageRegistrations();

        if(CollectionUtils.isEmpty(licenses))
            throw new CustomException("INVALID REQUEST","The request for calculation cannot be empty or null");

        CalculationRes response = getCalculation(requestInfo,licenses);
        List<Calculation> calculations = response.getCalculations();
        Map<String,Calculation> applicationNumberToCalculation = new HashMap<>();
        calculations.forEach(calculation -> {
            applicationNumberToCalculation.put(calculation.getMarriageRegistration().getApplicationNumber(),calculation);
            calculation.setMarriageRegistration(null);
        });

        licenses.forEach(license ->{
            license.setCalculation(applicationNumberToCalculation.get(license.getApplicationNumber()));
        });

        return licenses;
    }



    private CalculationRes getCalculation(RequestInfo requestInfo,List<MarriageRegistration> marriageRegistrations){
       // StringBuilder uri = utils.getCalculationURI(licenses.get(0).getBusinessService());
//        List<CalulationCriteria> criterias = new LinkedList<>();
//
//        licenses.forEach(license -> {
//            criterias.add(new CalulationCriteria(license,license.getApplicationNumber(),license.getTenantId()));
//        });
//
//        CalculationReq request = CalculationReq.builder().calulationCriteria(criterias)
//                .requestInfo(requestInfo)
//                .build();
//
//        Object result = serviceRequestRepository.fetchResult(uri,request);
        CalculationRes response = null;
        try{
        	response = new CalculationRes();
        	
        	List<Calculation> calculations = new ArrayList<>();
        	
        	Calculation calculation = new Calculation();
        	
        	List<TaxHeadEstimate> taxHeadEstimates = new ArrayList<>();
        	
        	TaxHeadEstimate taxHeadEstimate = new TaxHeadEstimate();
        	
        	taxHeadEstimate.setCategory(Category.TAX);
        	taxHeadEstimate.setEstimateAmount(new BigDecimal(100));
        	taxHeadEstimate.setTaxHeadCode("TL_TAX");
        	
        	taxHeadEstimates.add(taxHeadEstimate);
        	
        	calculation.setTaxHeadEstimates(taxHeadEstimates);
        	calculations.add(calculation);
        	calculation.setTenantId(marriageRegistrations.get(0).getTenantId());
        	
        	FeeAndBillingSlabIds feeAndBillingSlabIds = new FeeAndBillingSlabIds();
        	
        	feeAndBillingSlabIds.setFee(new BigDecimal(100));
        	
        	calculation.setTradeTypeBillingIds(feeAndBillingSlabIds);
        	
        	response.setCalculations(calculations);
        }
        catch (IllegalArgumentException e){
            throw new CustomException("PARSING ERROR","Failed to parse response of calculate");
        }
        return response;
    }
    
    

  
}
