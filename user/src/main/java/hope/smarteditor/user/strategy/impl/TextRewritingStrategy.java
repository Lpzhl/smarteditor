package hope.smarteditor.user.strategy.impl;


import hope.smarteditor.user.strategy.TextProcessingStrategy;
import org.springframework.stereotype.Component;

@Component("rewriting")
public class TextRewritingStrategy implements TextProcessingStrategy {
    private final String prompt = "你是一个智能文档生成器的AI，下面我将给你一句话：{text}，请帮我对上面的内容进行重写，只返回重写后的结果，不要返回多余的信息。";

    @Override
    public String generatePrompt(String userQuery) {
        return prompt.replace("{text}", userQuery);
    }
}
