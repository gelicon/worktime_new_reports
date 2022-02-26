package biz.gelicon.core.reports.engine;

public class ComboParamValue<T> {
    private T value;
    private String displayText;

    public ComboParamValue() {
    }

    public ComboParamValue(T value, String displayText) {
        this.value = value;
        this.displayText = displayText;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public String getDisplayText() {
        return displayText;
    }

    public void setDisplayText(String displayText) {
        this.displayText = displayText;
    }
}
