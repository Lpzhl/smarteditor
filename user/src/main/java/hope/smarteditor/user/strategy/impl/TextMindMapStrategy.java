package hope.smarteditor.user.strategy.impl;


import hope.smarteditor.user.strategy.TextProcessingStrategy;
import org.springframework.stereotype.Component;

@Component("mindmap")
public class TextMindMapStrategy implements TextProcessingStrategy {
    private final String promptTemplate = "下面我将给你一段文字，请根据这个文字生成Markdown格式的思维导图，不要返回多余的信息，什么多余的信息都不要返回，只要Markdown格式的数据。下面是这段文字：{text}";

    @Override
    public String generatePrompt(String userQuery) {
        return promptTemplate.replace("{text}", userQuery);
    }
}
