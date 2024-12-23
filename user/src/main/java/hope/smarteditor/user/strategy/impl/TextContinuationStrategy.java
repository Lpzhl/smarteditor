package hope.smarteditor.user.strategy.impl;


import hope.smarteditor.user.strategy.TextProcessingStrategy;
import org.springframework.stereotype.Component;

@Component("continuation")
public class TextContinuationStrategy implements TextProcessingStrategy {
    private final String promptStart = "你有着丰富的文本续写的经验，下面我将给你一句话：";
    private final String promptEnd = "请帮我对上面的句子进行文本续写至少200个字，只返回续写后的结果，不要返回多余的信息";

    @Override
    public String generatePrompt(String userQuery) {
        return promptStart + userQuery + promptEnd;
    }

}
