package hope.smarteditor.user.strategy.impl;

import hope.smarteditor.user.strategy.TextProcessingStrategy;
import org.springframework.stereotype.Component;

@Component("translation")
public class TextTranslationStrategy implements TextProcessingStrategy {
    private final String promptStart = "你是一个专业的中英文翻译专家，下面有一段文字：";
    private final String promptEnd = "请将上面的内容准确地翻译成英文。只返回翻译后的文本，不要添加额外的信息。";

    @Override
    public String generatePrompt(String userQuery) {
        return promptStart + userQuery + promptEnd;
    }
}
