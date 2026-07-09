package com.example.onlinelearningplatform.service;

import com.example.onlinelearningplatform.dto.SummaryResultDTO;
import com.example.onlinelearningplatform.dto.SummarySaveDTO;
import org.springframework.web.multipart.MultipartFile;

public interface SummaryService {
    SummaryResultDTO summarize(MultipartFile file);

    String save(SummarySaveDTO dto);
}
