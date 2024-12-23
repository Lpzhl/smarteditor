package hope.smarteditor.user.strategy.impl;


import hope.smarteditor.user.strategy.TextProcessingStrategy;
import org.springframework.stereotype.Component;

@Component("fix_format")
public class TextFormatFixStrategy implements TextProcessingStrategy {
    private final String promptTemplate = "你是一个智能文档生成器的AI，下面我将给你一段文字：{text}，请帮我对上面的文字进行格式优化，" +
            "要求是结构分层为三级分层,文本大小为正常,文本不加粗。格式优化后的结果用Markdown格式表示，只返回Markdown格式的结果，不要返回多余的信息。";

    @Override
    public String generatePrompt(String userQuery) {
        return promptTemplate.replace("{text}", userQuery);
    }
}
