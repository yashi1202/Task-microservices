package com.smarttask.collab.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
public class AddCommentRequest {

    @NotBlank(message = "Comment content is required")
    @Size(max = 5000,
          message = "Comment must not exceed 5000 characters")
    private String content;
}