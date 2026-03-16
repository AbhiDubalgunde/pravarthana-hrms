package com.pravarthana.hrms.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class CreateRoomRequest {

    @NotBlank(message = "Room name is required")
    private String name;

    /** 'group' or 'direct' */
    private String type = "group";

    /** User IDs to add as members (in addition to the creator) */
    private List<Long> memberIds;

    // Keep backward-compat field alias
    public String getRoomType() {
        return type;
    }
}
