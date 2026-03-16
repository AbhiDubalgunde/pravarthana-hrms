package com.pravarthana.hrms.dto.request;

import lombok.Data;

@Data
public class SendMessageRequest {
    private Long roomId;
    private String content;
    private Long companyId;    // set server-side; also sent from client for STOMP routing
    private Long senderId;     // set server-side from JWT
    private String senderName; // set server-side
}
