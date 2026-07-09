package com.example.onlinelearningplatform.service.impl;

import com.example.onlinelearningplatform.context.UserContext;
import com.example.onlinelearningplatform.dto.SummaryResultDTO;
import com.example.onlinelearningplatform.dto.SummarySaveDTO;
import com.example.onlinelearningplatform.entity.KnowledgeSummary;
import com.example.onlinelearningplatform.mapper.KnowledgeSummaryMapper;
import com.example.onlinelearningplatform.service.SummaryService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

// 上传文件让AI总结
@Service
@RequiredArgsConstructor
public class SummaryServiceImpl implements SummaryService {

    private static final long MAX_SIZE = 5 * 1024 * 1024;

    private final ChatClient.Builder chatClientBuilder;
    private final KnowledgeSummaryMapper summaryMapper;
    private final ObjectMapper objectMapper;

    @Override
    public SummaryResultDTO summarize(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("请选择文件");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new RuntimeException("文件大小不能超过5MB");
        }

        String fileName = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
        String text;
        try {
            if (fileName.endsWith(".txt") || fileName.endsWith(".md")) {
                text = new String(file.getBytes(), StandardCharsets.UTF_8);
            } else if (fileName.endsWith(".pdf")) {
                text = readPdf(file.getInputStream());
            } else {
                throw new RuntimeException("仅支持 txt、md、pdf 格式");
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("文件读取失败");
        }

        if (text.isBlank()) {
            throw new RuntimeException("文件内容为空");
        }
        if (text.length() > 15000) {
            text = text.substring(0, 15000);
        }

        String prompt = """
                请阅读以下学习资料并生成知识点总结，严格按JSON格式返回，不要包含其他内容：
                {"title":"总结标题","keyPoints":"关键点，用换行分隔","summaryContent":"详细摘要"}
                
                资料内容：
                """ + text;

        ChatClient chatClient = chatClientBuilder.build();
        String response = chatClient.prompt().user(prompt).call().content();
        return parseResult(response);
    }

    @Override
    public String save(SummarySaveDTO dto) {
        KnowledgeSummary summary = new KnowledgeSummary();
        summary.setUserId(UserContext.getCurrentUserId());
        summary.setCourseId(dto.getCourseId());
        summary.setTitle(dto.getTitle());
        summary.setKeyPoints(dto.getKeyPoints());
        summary.setSummaryContent(dto.getSummaryContent());
        summary.setCreatedTime(LocalDateTime.now());
        return summaryMapper.insert(summary) > 0 ? "保存成功" : "保存失败";
    }

    private SummaryResultDTO parseResult(String response) {
        SummaryResultDTO dto = new SummaryResultDTO();
        try {
            int start = response.indexOf('{');
            int end = response.lastIndexOf('}');
            String json = start >= 0 && end > start ? response.substring(start, end + 1) : response;
            JsonNode node = objectMapper.readTree(json);
            dto.setTitle(node.path("title").asText("知识点总结"));
            dto.setKeyPoints(node.path("keyPoints").asText());
            dto.setSummaryContent(node.path("summaryContent").asText());
        } catch (Exception e) {
            dto.setTitle("知识点总结");
            dto.setKeyPoints("解析失败，请查看详细摘要");
            dto.setSummaryContent(response);
        }
        return dto;
    }

    private String readPdf(InputStream inputStream) throws Exception {
        try (PDDocument document = Loader.loadPDF(inputStream.readAllBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }
}
