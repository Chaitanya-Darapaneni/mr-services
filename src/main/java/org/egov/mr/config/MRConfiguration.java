package org.egov.mr.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;
import org.egov.tracer.config.TracerConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.TimeZone;


@Import({TracerConfiguration.class})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Component
public class MRConfiguration {



    // User Config
    @Value("${egov.user.host}")
    private String userHost;

    @Value("${egov.user.context.path}")
    private String userContextPath;

    @Value("${egov.user.create.path}")
    private String userCreateEndpoint;

    @Value("${egov.user.search.path}")
    private String userSearchEndpoint;

    @Value("${egov.user.update.path}")
    private String userUpdateEndpoint;

    @Value("${egov.user.username.prefix}")
    private String usernamePrefix;


    //Idgen Config
    @Value("${egov.idgen.host}")
    private String idGenHost;

    @Value("${egov.idgen.path}")
    private String idGenPath;

    @Value("${egov.idgen.mr.applicationNum.name}")
    private String applicationNumberIdgenNameMR;

    @Value("${egov.idgen.mr.applicationNum.format}")
    private String applicationNumberIdgenFormatMR;

    @Value("${egov.idgen.mr.licensenumber.name}")
    private String mrNumberIdgenNameMR;

    @Value("${egov.idgen.mr.licensenumber.format}")
    private String mrNumberIdgenFormatMR;

  

    //Persister Config
    @Value("${persister.save.tradelicense.topic}")
    private String saveTopic;

    @Value("${persister.update.tradelicense.topic}")
    private String updateTopic;

    @Value("${persister.update.tradelicense.workflow.topic}")
    private String updateWorkflowTopic;



    //Location Config
    @Value("${egov.location.host}")
    private String locationHost;

    @Value("${egov.location.context.path}")
    private String locationContextPath;

    @Value("${egov.location.endpoint}")
    private String locationEndpoint;

    @Value("${egov.location.hierarchyTypeCode}")
    private String hierarchyTypeCode;

    @Value("${egov.mr.default.limit}")
    private Integer defaultLimit;

    @Value("${egov.mr.default.offset}")
    private Integer defaultOffset;

    @Value("${egov.mr.max.limit}")
    private Integer maxSearchLimit;



    @Value("${egov.billingservice.host}")
    private String billingHost;

    @Value("${egov.bill.gen.endpoint}")
    private String fetchBillEndpoint;




    //Localization
    @Value("${egov.localization.host}")
    private String localizationHost;

    @Value("${egov.localization.context.path}")
    private String localizationContextPath;

    @Value("${egov.localization.search.endpoint}")
    private String localizationSearchEndpoint;

    @Value("${egov.localization.statelevel}")
    private Boolean isLocalizationStateLevel;



    //MDMS
    @Value("${egov.mdms.host}")
    private String mdmsHost;

    @Value("${egov.mdms.search.endpoint}")
    private String mdmsEndPoint;



    @Value("${workflow.context.path}")
    private String wfHost;

    @Value("${workflow.transition.path}")
    private String wfTransitionPath;

    @Value("${workflow.businessservice.search.path}")
    private String wfBusinessServiceSearchPath;



}
