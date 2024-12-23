package hope.smarteditor.user.strategy.impl;


import hope.smarteditor.user.strategy.TextProcessingStrategy;
import org.springframework.stereotype.Component;

@Component("summarization")
public class TextSummarizationStrategy implements TextProcessingStrategy {
    private final String promptStart = "你是一个有着丰富文本内容概要提取经验，下面我将给你一段话：";
    private final String promptEnd = "请帮我对上面的内容进行内容概要提取，只返回提取后的结果，不要返回多余的信息";

    @Override
    public String generatePrompt(String userQuery) {
        return promptStart + userQuery + promptEnd;
    }
}
