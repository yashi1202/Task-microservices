package com.smarttask.task.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ApproveTaskRequest {

	  @NotNull(message = "Version is required")
	  //@JsonProperty("version")
	    private Long version;

	    // Admin's review comment — required for approval
	    @NotNull(message = "Review comment is required")
	    //@JsonProperty("reviewComment")
	    private String reviewComment;
}