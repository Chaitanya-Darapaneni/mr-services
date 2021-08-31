package org.egov.mr.web.models;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GuardianDetails extends CoupleAddress{
	
	@Size(max=64)
    @JsonProperty("groomSideGuardian")
	private boolean groomSideGuardian ;
	
	@Size(max=64)
    @JsonProperty("relationship")
	private String relationship ;
	
	@Size(max=64)
    @JsonProperty("name")
	private String name ;
	
	
    @Pattern(regexp = "^[6-9][0-9]{9}$", message = "Invalid mobile number")
    @JsonProperty("contact")
    private String contact;

    @Size(max=128)
    @JsonProperty("emailAddress")
    @Pattern(regexp = "^$|^(?=^.{1,64}$)((([^<>()\\[\\]\\\\.,;:\\s$*@'\"]+(\\.[^<>()\\[\\]\\\\.,;:\\s@'\"]+)*)|(\".+\"))@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,})))$", message = "Invalid emailId")
    private String emailAddress;

}
