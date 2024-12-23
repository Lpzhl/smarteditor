package hope.smarteditor.user.strategy.impl;


import hope.smarteditor.user.strategy.TextProcessingStrategy;
import org.springframework.stereotype.Component;

@Component("correction")
public class TextCorrectionStrategy implements TextProcessingStrategy {
    private final String promptStart = "你有着丰富的文本校正经验，下面我讲给你一段话:";
    private final String promptEnd = "请帮我对上面的句子进行文本校正不要有多余的话，这里的校正包括纠正错别字，还有优化表达方式，纠正错误的标点符号，校正后的句子要更加流畅，返回的结果格式是" +
            "修正后的文本内容：XXX" +
            "修改说明：XXX";

    @Override
    public String generatePrompt(String userQuery) {
        return promptStart + userQuery + promptEnd;
    }
}
