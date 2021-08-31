package org.egov.mr.service;

import org.egov.common.contract.request.RequestInfo;
import org.egov.mr.config.MRConfiguration;
import org.egov.mr.repository.IdGenRepository;
import org.egov.mr.util.MarriageRegistrationUtil;
import org.egov.mr.web.models.AuditDetails;
import org.egov.mr.web.models.MarriageRegistration;
import org.egov.mr.web.models.MarriageRegistrationRequest;
import org.egov.mr.web.models.Idgen.IdResponse;
import org.egov.mr.workflow.WorkflowService;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import com.jayway.jsonpath.JsonPath;

import java.util.*;
import java.util.stream.Collectors;
import static org.egov.mr.util.MRConstants.*;


@Service
public class EnrichmentService {

    private IdGenRepository idGenRepository;
    private MRConfiguration config;
    private MarriageRegistrationUtil marriageRegistrationUtil;
    private BoundaryService boundaryService;
    private WorkflowService workflowService;

    @Autowired
    public EnrichmentService(IdGenRepository idGenRepository, MRConfiguration config,
                             BoundaryService boundaryService,WorkflowService workflowService) {
        this.idGenRepository = idGenRepository;
        this.config = config;
        this.boundaryService = boundaryService;
        this.workflowService = workflowService;
    }


    /**
     * Enriches the incoming createRequest
     * @param marriageRegistrationRequest The create request for the maariageRegistration
     */
    public void enrichMRCreateRequest(MarriageRegistrationRequest marriageRegistrationRequest) {
        RequestInfo requestInfo = marriageRegistrationRequest.getRequestInfo();
        String uuid = requestInfo.getUserInfo().getUuid();
        AuditDetails auditDetails = marriageRegistrationUtil.getAuditDetails(uuid, true);
        marriageRegistrationRequest.getMarriageRegistrations().forEach(marriageRegistration -> {
            marriageRegistration.setAuditDetails(auditDetails);
            marriageRegistration.setId(UUID.randomUUID().toString());
            marriageRegistration.setApplicationDate(auditDetails.getCreatedTime());
            marriageRegistration.getMarriagePlace().setId(UUID.randomUUID().toString());
            marriageRegistration.getMarriagePlace().setAuditDetails(auditDetails);
            String businessService = marriageRegistration.getBusinessService();
            if (businessService == null)
            {
                businessService = businessService_MR;
                marriageRegistration.setBusinessService(businessService);
            }
           
            marriageRegistration.getCoupleDetails().forEach(couple -> {
            	couple.setTenantId(marriageRegistration.getTenantId());
            	couple.setId(UUID.randomUUID().toString());
            });
          

            
     

            if (requestInfo.getUserInfo().getType().equalsIgnoreCase("CITIZEN"))
                marriageRegistration.setAccountId(requestInfo.getUserInfo().getUuid());

        });
        
        
        
        setIdgenIds(marriageRegistrationRequest);
        setStatusForCreate(marriageRegistrationRequest);
        String businessService = marriageRegistrationRequest.getMarriageRegistrations().isEmpty()?null:marriageRegistrationRequest.getMarriageRegistrations().get(0).getBusinessService();
        if (businessService == null)
            businessService = businessService_MR;
        switch (businessService) {
            case businessService_MR:
                boundaryService.getAreaType(marriageRegistrationRequest, config.getHierarchyTypeCode());
                break;
        }
    }


    /**
     * Returns a list of numbers generated from idgen
     *
     * @param requestInfo RequestInfo from the request
     * @param tenantId    tenantId of the city
     * @param idKey       code of the field defined in application properties for which ids are generated for
     * @param idformat    format in which ids are to be generated
     * @param count       Number of ids to be generated
     * @return List of ids generated using idGen service
     */
    private List<String> getIdList(RequestInfo requestInfo, String tenantId, String idKey,
                                   String idformat, int count) {
        List<IdResponse> idResponses = idGenRepository.getId(requestInfo, tenantId, idKey, idformat, count).getIdResponses();

        if (CollectionUtils.isEmpty(idResponses))
            throw new CustomException("IDGEN ERROR", "No ids returned from idgen Service");

        return idResponses.stream()
                .map(IdResponse::getId).collect(Collectors.toList());
    }


    /**
     * Sets the ApplicationNumber for given TradeLicenseRequest
     *
     * @param request TradeLicenseRequest which is to be created
     */
    private void setIdgenIds(MarriageRegistrationRequest request) {
        RequestInfo requestInfo = request.getRequestInfo();
        String tenantId = request.getMarriageRegistrations().get(0).getTenantId();
        List<MarriageRegistration> marriageRegistrations = request.getMarriageRegistrations();
        String businessService = marriageRegistrations.isEmpty() ? null : marriageRegistrations.get(0).getBusinessService();
        if (businessService == null)
            businessService = businessService_MR;
        List<String> applicationNumbers = null;
        switch (businessService) {
            case businessService_MR:
                applicationNumbers = getIdList(requestInfo, tenantId, config.getApplicationNumberIdgenNameMR(), config.getApplicationNumberIdgenFormatMR(), request.getMarriageRegistrations().size());
                break;

        }
        ListIterator<String> itr = applicationNumbers.listIterator();

        Map<String, String> errorMap = new HashMap<>();
        if (applicationNumbers.size() != request.getMarriageRegistrations().size()) {
            errorMap.put("IDGEN ERROR ", "The number of LicenseNumber returned by idgen is not equal to number of TradeLicenses");
        }

        if (!errorMap.isEmpty())
            throw new CustomException(errorMap);

        marriageRegistrations.forEach(tradeLicense -> {
            tradeLicense.setApplicationNumber(itr.next());
        });
    }


    /**
     * Adds the ownerIds from userSearchReponse to search criteria
     * @param criteria The TradeLicense search Criteria
     * @param userDetailResponse The response of user search
     */
    public void enrichTLCriteriaWithOwnerids(TradeLicenseSearchCriteria criteria, UserDetailResponse userDetailResponse){
        if(CollectionUtils.isEmpty(criteria.getOwnerIds())){
            Set<String> ownerids = new HashSet<>();
            userDetailResponse.getUser().forEach(owner -> ownerids.add(owner.getUuid()));
            criteria.setOwnerIds(new ArrayList<>(ownerids));
        }
    }


    /**
     * Creates search criteria of only the tradeLicense ids
     * @param licenses The licenses whose ids are to extracted
     * @return The search criteria
     */
    public TradeLicenseSearchCriteria getTLSearchCriteriaFromTLIds(List<TradeLicense> licenses){
        TradeLicenseSearchCriteria criteria = new TradeLicenseSearchCriteria();
        List<String> ids = new ArrayList<>();
        licenses.forEach(license -> ids.add(license.getId()));
        criteria.setIds(ids);
        criteria.setTenantId(licenses.get(0).getTenantId());
        criteria.setBusinessService(licenses.get(0).getBusinessService());
        return criteria;
    }


    /**
     * Enriches search criteria with ownerIds from license
     * @param criteria TradeLicense search criteria
     * @param licenses The tradeLicense whose owners are to be enriched
     */
    public TradeLicenseSearchCriteria enrichTLSearchCriteriaWithOwnerids(TradeLicenseSearchCriteria criteria, List<TradeLicense> licenses) {
        TradeLicenseSearchCriteria searchCriteria = new TradeLicenseSearchCriteria();
        searchCriteria.setTenantId(criteria.getTenantId());
        Set<String> ownerids = new HashSet<>();
        licenses.forEach(license -> {
            license.getTradeLicenseDetail().getOwners().forEach(owner -> ownerids.add(owner.getUuid()));
        });

      /*  licenses.forEach(tradeLicense -> {
            ownerids.add(tradeLicense.getCitizenInfo().getUuid());
            });*/
        searchCriteria.setBusinessService(licenses.get(0).getBusinessService());
        searchCriteria.setOwnerIds(new ArrayList<>(ownerids));
        return searchCriteria;
    }



    /**
     * Enriches the boundary object in address
     * @param tradeLicenseRequest The create request
     */
    public void enrichBoundary(TradeLicenseRequest tradeLicenseRequest){
        List<TradeLicenseRequest> requests = getRequestByTenantId(tradeLicenseRequest);
        requests.forEach(tenantWiseRequest -> {
           boundaryService.getAreaType(tenantWiseRequest,config.getHierarchyTypeCode());
        });
    }


    /**
     *
     * @param request
     * @return
     */
    private List<TradeLicenseRequest> getRequestByTenantId(TradeLicenseRequest request){
        List<TradeLicense> licenses = request.getLicenses();
        RequestInfo requestInfo = request.getRequestInfo();

        Map<String,List<TradeLicense>> tenantIdToProperties = new HashMap<>();
        if(!CollectionUtils.isEmpty(licenses)){
            licenses.forEach(license -> {
                if(tenantIdToProperties.containsKey(license.getTenantId()))
                    tenantIdToProperties.get(license.getTenantId()).add(license);
                else{
                    List<TradeLicense> list = new ArrayList<>();
                    list.add(license);
                    tenantIdToProperties.put(license.getTenantId(),list);
                }
            });
        }
        List<TradeLicenseRequest> requests = new LinkedList<>();

        tenantIdToProperties.forEach((key,value)-> {
            requests.add(new TradeLicenseRequest(requestInfo,value));
        });
        return requests;
    }



    /**
     * Enriches the owner fields from user search response
     * @param userDetailResponse user search response
     * @param licenses licenses whose owners are to be enriches
     */
    public void enrichOwner(UserDetailResponse userDetailResponse, List<TradeLicense> licenses){
        List<OwnerInfo> users = userDetailResponse.getUser();
        Map<String,OwnerInfo> userIdToOwnerMap = new HashMap<>();
        users.forEach(user -> userIdToOwnerMap.put(user.getUuid(),user));
        licenses.forEach(license -> {
            license.getTradeLicenseDetail().getOwners().forEach(owner -> {
                    if(userIdToOwnerMap.get(owner.getUuid())==null)
                        throw new CustomException("OWNER SEARCH ERROR","The owner of the tradeCategoryDetail "+license.getTradeLicenseDetail().getId()+" is not coming in user search");
                    else
                        owner.addUserDetail(userIdToOwnerMap.get(owner.getUuid()));
                 });

           /* if(userIdToOwnerMap.get(license.getCitizenInfo().getUuid())!=null)
                license.getCitizenInfo().addCitizenDetail(userIdToOwnerMap.get(license.getCitizenInfo().getUuid()));
            else
                throw new CustomException("CITIZENINFO ERROR","The citizenInfo of trade License with ApplicationNumber: "+license.getApplicationNumber()+" cannot be found");
*/
        });
    }


    /**
     * Sets status for create request
     * @param marriageRegistrationRequest The create request
     */
    private void setStatusForCreate(MarriageRegistrationRequest marriageRegistrationRequest) {
        marriageRegistrationRequest.getMarriageRegistrations().forEach(marriageRegistration -> {
            String businessService = marriageRegistrationRequest.getMarriageRegistrations().isEmpty()?null:marriageRegistrationRequest.getMarriageRegistrations().get(0).getBusinessService();
            if (businessService == null)
                businessService = businessService_MR;
            switch (businessService) {
                case businessService_MR:
                    if (marriageRegistration.getAction().equalsIgnoreCase(ACTION_INITIATE))
                        marriageRegistration.setStatus(STATUS_INITIATED);
                    if (marriageRegistration.getAction().equalsIgnoreCase(ACTION_APPLY))
                        marriageRegistration.setStatus(STATUS_APPLIED);
                    break;

                
            }
        });
    }


    /**
     * Enriches the update request
     * @param tradeLicenseRequest The input update request
     */
    public void enrichTLUpdateRequest(TradeLicenseRequest tradeLicenseRequest, BusinessService businessService){
        RequestInfo requestInfo = tradeLicenseRequest.getRequestInfo();
        AuditDetails auditDetails = tradeUtil.getAuditDetails(requestInfo.getUserInfo().getUuid(), false);
        tradeLicenseRequest.getLicenses().forEach(tradeLicense -> {
            tradeLicense.setAuditDetails(auditDetails);
            enrichAssignes(tradeLicense);
            String nameOfBusinessService = tradeLicense.getBusinessService();
            if(nameOfBusinessService==null)
            {
                nameOfBusinessService=businessService_TL;
                tradeLicense.setBusinessService(nameOfBusinessService);
            }
            if ((nameOfBusinessService.equals(businessService_BPA) && (tradeLicense.getStatus().equalsIgnoreCase(STATUS_INITIATED))) || workflowService.isStateUpdatable(tradeLicense.getStatus(), businessService)) {
                tradeLicense.getTradeLicenseDetail().setAuditDetails(auditDetails);
                if (!CollectionUtils.isEmpty(tradeLicense.getTradeLicenseDetail().getAccessories())) {
                    tradeLicense.getTradeLicenseDetail().getAccessories().forEach(accessory -> {
                        if (accessory.getId() == null) {
                            accessory.setTenantId(tradeLicense.getTenantId());
                            accessory.setId(UUID.randomUUID().toString());
                            accessory.setActive(true);
                        }
                    });
                }

                tradeLicense.getTradeLicenseDetail().getTradeUnits().forEach(tradeUnit -> {
                    if (tradeUnit.getId() == null) {
                        tradeUnit.setTenantId(tradeLicense.getTenantId());
                        tradeUnit.setId(UUID.randomUUID().toString());
                        tradeUnit.setActive(true);
                    }
                });

                tradeLicense.getTradeLicenseDetail().getOwners().forEach(owner -> {
                    if(owner.getUuid()==null || owner.getUserActive()==null)
                        owner.setUserActive(true);
                    if (!CollectionUtils.isEmpty(owner.getDocuments()))
                        owner.getDocuments().forEach(document -> {
                            if (document.getId() == null) {
                                document.setId(UUID.randomUUID().toString());
                                document.setActive(true);
                            }
                        });
                });

                if(tradeLicense.getTradeLicenseDetail().getSubOwnerShipCategory().contains(config.getInstitutional())
                        && tradeLicense.getTradeLicenseDetail().getInstitution().getId()==null){
                    tradeLicense.getTradeLicenseDetail().getInstitution().setId(UUID.randomUUID().toString());
                    tradeLicense.getTradeLicenseDetail().getInstitution().setActive(true);
                    tradeLicense.getTradeLicenseDetail().getInstitution().setTenantId(tradeLicense.getTenantId());
                    tradeLicense.getTradeLicenseDetail().getOwners().forEach(owner -> {
                        owner.setInstitutionId(tradeLicense.getTradeLicenseDetail().getInstitution().getId());
                    });
                }

                if(!CollectionUtils.isEmpty(tradeLicense.getTradeLicenseDetail().getApplicationDocuments())){
                    tradeLicense.getTradeLicenseDetail().getApplicationDocuments().forEach(document -> {
                        if(document.getId()==null){
                            document.setId(UUID.randomUUID().toString());
                            document.setActive(true);
                        }
                    });
                }
            }
            else {
                if(!CollectionUtils.isEmpty(tradeLicense.getTradeLicenseDetail().getVerificationDocuments())){
                    tradeLicense.getTradeLicenseDetail().getVerificationDocuments().forEach(document -> {
                        if(document.getId()==null){
                            document.setId(UUID.randomUUID().toString());
                            document.setActive(true);
                        }
                    });
                }
            }
        });
    }

    /**
     * Sets the licenseNumber generated by idgen
     * @param request The update request
     */
    private void setLicenseNumberAndIssueDate(TradeLicenseRequest request,List<String>endstates , Object mdmsData) {
        RequestInfo requestInfo = request.getRequestInfo();
        String tenantId = request.getLicenses().get(0).getTenantId();
        List<TradeLicense> licenses = request.getLicenses();
        int count=0;
        
        
        if (licenses.get(0).getApplicationType() != null && licenses.get(0).getApplicationType().toString().equals(TLConstants.APPLICATION_TYPE_RENEWAL)) {
            for(int i=0;i<licenses.size();i++){
                TradeLicense license = licenses.get(i);
                Long time = System.currentTimeMillis();
                license.setIssuedDate(time);
            }
        }else {
            for (int i = 0; i < licenses.size(); i++) {
                TradeLicense license = licenses.get(i);
                if ((license.getStatus() != null) && license.getStatus().equalsIgnoreCase(endstates.get(i)))
                    count++;
            }
            if (count != 0) {
                List<String> licenseNumbers = null;
                String businessService = licenses.isEmpty() ? null : licenses.get(0).getBusinessService();
                if (businessService == null)
                    businessService = businessService_TL;
                switch (businessService) {
                    case businessService_TL:
                        licenseNumbers = getIdList(requestInfo, tenantId, config.getLicenseNumberIdgenNameTL(), config.getLicenseNumberIdgenFormatTL(), count);
                        break;

                    case businessService_BPA:
                        licenseNumbers = getIdList(requestInfo, tenantId, config.getLicenseNumberIdgenNameBPA(), config.getLicenseNumberIdgenFormatBPA(), count);
                        break;
                }
                ListIterator<String> itr = licenseNumbers.listIterator();

                Map<String, String> errorMap = new HashMap<>();
                if (licenseNumbers.size() != count) {
                    errorMap.put("IDGEN ERROR ", "The number of LicenseNumber returned by idgen is not equal to number of TradeLicenses");
                }

                if (!errorMap.isEmpty())
                    throw new CustomException(errorMap);

                for (int i = 0; i < licenses.size(); i++) {
                    TradeLicense license = licenses.get(i);
                    if ((license.getStatus() != null) && license.getStatus().equalsIgnoreCase(endstates.get(i))) {
                        license.setLicenseNumber(itr.next());
                        Long time = System.currentTimeMillis();
                        license.setIssuedDate(time);
                        //license.setValidFrom(time);
                        if (mdmsData != null && businessService.equalsIgnoreCase(businessService_BPA)) {
                            String jsonPath = TLConstants.validityPeriodMap.replace("{}",
                                    license.getTradeLicenseDetail().getTradeUnits().get(0).getTradeType());
                            List<Integer> res = JsonPath.read(mdmsData, jsonPath);
                            Calendar calendar = Calendar.getInstance();
                            calendar.add(Calendar.YEAR, res.get(0));
                            license.setValidTo(calendar.getTimeInMillis());
                            license.setValidFrom(time);
                        }

                    }
                }
            }

        }
    }


    /**
     * Adds accountId of the logged in user to search criteria
     * @param requestInfo The requestInfo of searhc request
     * @param criteria The tradeLicenseSearch criteria
     */
    public void enrichSearchCriteriaWithAccountId(RequestInfo requestInfo,TradeLicenseSearchCriteria criteria){
        if(criteria.isEmpty() && requestInfo.getUserInfo().getType().equalsIgnoreCase("CITIZEN")){
            criteria.setAccountId(requestInfo.getUserInfo().getUuid());
            criteria.setMobileNumber(requestInfo.getUserInfo().getUserName());
            criteria.setTenantId(requestInfo.getUserInfo().getTenantId());
        }

    }

    /**
     * Enriches the tradeLicenses with ownerInfo and Boundary data
     * @param licenses The licenses to be enriched
     * @param criteria The search criteria of licenses containing the ownerIds
     * @param requestInfo The requestInfo of search
     * @return enriched tradeLicenses
     */
    public List<TradeLicense> enrichTradeLicenseSearch(List<TradeLicense> licenses, TradeLicenseSearchCriteria criteria, RequestInfo requestInfo){

        String businessService = licenses.isEmpty()?null:licenses.get(0).getBusinessService();
        if (businessService == null)
            businessService = businessService_TL;
        TradeLicenseSearchCriteria searchCriteria = enrichTLSearchCriteriaWithOwnerids(criteria,licenses);
        switch (businessService) {
            case businessService_TL:
                enrichBoundary(new TradeLicenseRequest(requestInfo, licenses));
                break;
        }
        UserDetailResponse userDetailResponse = userService.getUser(searchCriteria,requestInfo);
        enrichOwner(userDetailResponse,licenses);
        return licenses;
    }


    /**
     * Enriches the object after status is assigned
     * @param tradeLicenseRequest The update request
     */
    public void postStatusEnrichment(TradeLicenseRequest tradeLicenseRequest,List<String>endstates, Object mdmsData){
        setLicenseNumberAndIssueDate(tradeLicenseRequest,endstates,mdmsData);
    }


    /**
     * Creates search criteria from list of trade license
     * @param licenses The licenses whose ids are to be added to search
     * @return tradeLicenseSearch criteria on basis of tradelicense id
     */
    public TradeLicenseSearchCriteria getTradeLicenseCriteriaFromIds(List<TradeLicense> licenses){
        TradeLicenseSearchCriteria criteria = new TradeLicenseSearchCriteria();
        Set<String> licenseIds = new HashSet<>();
        licenses.forEach(license -> licenseIds.add(license.getId()));
        criteria.setIds(new LinkedList<>(licenseIds));
        criteria.setBusinessService(licenses.get(0).getBusinessService());
        return criteria;
    }

    /**
     * In case of SENDBACKTOCITIZEN enrich the assignee with the owners and creator of license
     * @param license License to be enriched
     */
    public void enrichAssignes(TradeLicense license){

            if(license.getAction().equalsIgnoreCase(CITIZEN_SENDBACK_ACTION)){

                    Set<String> assignes = new HashSet<>();

                    // Adding owners to assignes list
                    license.getTradeLicenseDetail().getOwners().forEach(ownerInfo -> {
                       assignes.add(ownerInfo.getUuid());
                    });

                    // Adding creator of license
                    if(license.getAccountId()!=null)
                        assignes.add(license.getAccountId());

                    Set<String> registeredUUIDS = userService.getUUidFromUserName(license);

                    if(!CollectionUtils.isEmpty(registeredUUIDS))
                        assignes.addAll(registeredUUIDS);


                    license.setAssignee(new LinkedList<>(assignes));
            }
    }




}
