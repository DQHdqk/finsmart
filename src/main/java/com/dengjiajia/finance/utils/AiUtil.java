package com.dengjiajia.finance.utils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class AiUtil {

    @Value("${finsmart.ai.api-key}")
    private String apiKey;

    @Value("${finsmart.ai.url}")
    private String apiUrl;

    @Value("${finsmart.ai.model}")
    private String model;

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build();

    // 纯文字对话
    public String chat(String prompt) {
        JSONArray messages = new JSONArray();
        JSONObject message = new JSONObject();
        message.put("role", "user");
        message.put("content", prompt);
        messages.add(message);
        return callApi(messages);
    }

    // 图片识别（支持base64图片）
    public String chatWithImage(String prompt, String imageBase64) {
        JSONArray messages = new JSONArray();
        JSONObject message = new JSONObject();
        message.put("role", "user");

        // 构建多模态内容
        JSONArray content = new JSONArray();

        // 添加图片
        JSONObject imageContent = new JSONObject();
        imageContent.put("type", "image_url");
        JSONObject imageUrl = new JSONObject();
        imageUrl.put("url", "data:image/jpeg;base64," + imageBase64);
        imageContent.put("image_url", imageUrl);
        content.add(imageContent);

        // 添加文字提示
        JSONObject textContent = new JSONObject();
        textContent.put("type", "text");
        textContent.put("text", prompt);
        content.add(textContent);

        message.put("content", content);
        messages.add(message);

        // 图片识别用glm-4v模型
        return callApi(messages, "glm-4v-flash");
    }

    private String callApi(JSONArray messages) {
        return callApi(messages, model);
    }

    private String callApi(JSONArray messages, String modelName) {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", modelName);
            requestBody.put("messages", messages);

            RequestBody body = RequestBody.create(
                    requestBody.toJSONString(),
                    MediaType.parse("application/json")
            );

            Request request = new Request.Builder()
                    .url(apiUrl)
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .post(body)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    log.error("AI接口调用失败: {} {}", response.code(),
                            response.body() != null ? response.body().string() : "");
                    return "AI分析服务暂时不可用";
                }
                String responseBody = response.body().string();
                JSONObject result = JSON.parseObject(responseBody);
                return result.getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content");
            }
        } catch (Exception e) {
            log.error("AI分析异常: {}", e.getMessage());
            return "AI分析服务暂时不可用";
        }
    }
}


