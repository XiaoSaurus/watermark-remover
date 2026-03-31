package com.watermark.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class WatermarkTask {
    private String id;
    private String originalFileName;
    private String fileType;   // image / video
    private String status;     // pending / processing / done / failed
    private String outputPath;
    private String errorMsg;
    private LocalDateTime createdAt;
    private LocalDateTime finishedAt;
}