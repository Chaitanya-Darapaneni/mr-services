package org.egov.mr.web.models.calculation;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import org.egov.mr.web.models.MarriageRegistration;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * Calculation
 */
@Validated
@javax.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2018-09-27T14:56:03.454+05:30")

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Calculation {

	@JsonProperty("applicationNumber")
	private String applicationNumber = null;

	@JsonProperty("marriageRegistration")
	private MarriageRegistration marriageRegistration = null;

	@NotNull
	@JsonProperty("tenantId")
	@Size(min = 2, max = 256)
	private String tenantId = null;

	@JsonProperty("taxHeadEstimates")
	List<TaxHeadEstimate> taxHeadEstimates;

	@JsonProperty("tradeTypeBillingIds")
	FeeAndBillingSlabIds tradeTypeBillingIds;

	@JsonProperty("accessoryBillingIds")
	FeeAndBillingSlabIds accessoryBillingIds;

}