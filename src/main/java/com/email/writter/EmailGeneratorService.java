package com.email.writter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;



@Service
public class EmailGeneratorService {

    private final WebClient webClient;

    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    public EmailGeneratorService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }


    public String generateEmailReply(EmailRequest emailRequest) {
         //build prompt
        String prompt = buildPrompt(emailRequest);

        //craft request
        Map<String,Object> requestBody = Map.of(
                 "contents",new Object[]{
                         Map.of("parts",new Object[]{
                                 Map.of("text",prompt)
                         })
                }
        );

        // do req and get response
    String apiUrlWithKey = geminiApiUrl + "?key=" + geminiApiKey;
    String response = webClient.post()
        .uri(apiUrlWithKey)
        .header("Content-Type","application/json")
        .bodyValue(requestBody)
        .retrieve()
        .bodyToMono(String.class)
        .block();
        //extracr res and return
        return extractResponseContent(response);
    }

    private String extractResponseContent(String response) {
        try{
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode =mapper.readTree(response);
            return rootNode.path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText();
        }
        catch(Exception e) {
            return "Error process req"+e.getMessage();
        }
    }

    private String buildPrompt(EmailRequest emailRequest) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Generate a professional email reply for following email content.dont generate subject line");
        if(emailRequest.getTone()!=null && !emailRequest.getTone().isEmpty()) {
            prompt.append("Use a").append(emailRequest.getTone()).append(" Tone");
        }
        prompt.append("\nOriginal email\n").append(emailRequest.getEmailContent());

        return prompt.toString();
    }
}
