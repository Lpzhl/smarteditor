package hope.smarteditor.common.model.dto;

public class FontSettingDTO {
    private String fontFamily;  // 字体类型
    private int fontSize;       // 字体大小
    private float lineHeight;   // 行间距

    // Getters and Setters
    public String getFontFamily() {
        return fontFamily;
    }

    public void setFontFamily(String fontFamily) {
        this.fontFamily = fontFamily;
    }

    public int getFontSize() {
        return fontSize;
    }

    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }

    public float getLineHeight() {
        return lineHeight;
    }

    public void setLineHeight(float lineHeight) {
        this.lineHeight = lineHeight;
    }
}
