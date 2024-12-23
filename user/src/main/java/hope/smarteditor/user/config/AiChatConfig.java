package hope.smarteditor.user.config;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiChatConfig {

    @Value("${ai-chat.apiKey}")
    private String apiKey;

    @Value("${ai-chat.url}")
    private String apiUrl;

    @Value("${ai-chat.model}")
    private String model;

    @Bean
    public ChatLanguageModel chatLanguageModel() {
        return OpenAiChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(apiUrl)
                .modelName(model)
                .build();
    }

    @Bean
    public StreamingChatLanguageModel streamingLanguageModel(){
        return OpenAiStreamingChatModel.builder()
                .baseUrl("https://dashscope.aliyuncs.com/compatible-mode/v1")
                .apiKey("sk-6d32fe03e3c7462cbd6a32d87a1181a0")
                .modelName("qwq-32b-preview")
                .build();
    }
}
