package biz.gelicon.core.reports.engine;

import org.eclipse.birt.report.engine.api.IGetParameterDefinitionTask;
import org.eclipse.birt.report.engine.api.IParameterSelectionChoice;

import java.util.Collection;
import java.util.HashMap;

/**
 * Метаданные для параметра отчета birt
 */
public class BirtParamMetadata {

    private String parameterName; // Имя параметра
    private Object value; // Значение параметра, не используется, на будущее
    private String defaultValue; // Значение параметра по умолчанию
    private String label; // Метка параметра, не используется, на будущее
    private Integer parameterControlType; // Тип вбивалки параметра
    private Integer parameterDataType; // Тип параметра
    private HashMap<Object, String> dynamicList; // Выпадающий список value, label

    public BirtParamMetadata(String parameterName) {
        this.parameterName = parameterName;
        this.dynamicList = new HashMap<>();
    }

    public BirtParamMetadata(
            String parameterName,
            Object value,
            String label,
            Integer parameterControlType,
            Integer parameterDataType,
            HashMap<Object, String> dynamicList
    ) {
        this.parameterName = parameterName;
        this.value = value;
        this.label = label;
        this.parameterControlType = parameterControlType;
        this.parameterDataType = parameterDataType;
        this.dynamicList = dynamicList;
    }

    public String getParameterName() {
        return parameterName;
    }

    public Object getValue() {
        return value;
    }

    public String getLabel() {
        return label;
    }

    public Integer getParameterControlType() {
        return parameterControlType;
    }

    public Integer getParameterDataType() {
        return parameterDataType;
    }

    public HashMap<Object, String> getDynamicList() {
        return dynamicList;
    }

    public void setParameterName(String parameterName) {
        this.parameterName = parameterName;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setParameterControlType(Integer parameterControlType) {
        this.parameterControlType = parameterControlType;
    }

    public void setParameterDataType(Integer parameterDataType) {
        this.parameterDataType = parameterDataType;
    }

    public void setDynamicList(HashMap<Object, String> dynamicList) {
        this.dynamicList = dynamicList;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * Заполнение списка значений для параметра
     * @param task задача
     * @param parameterName
     *
     * Поле dynamicList всегда не null после этого
     * Задача из дизайнера движком получается так (справочно):
     * IGetParameterDefinitionTask task = eng.createGetParameterDefinitionTask(des);
     */
    public void buildDynamicList(
            IGetParameterDefinitionTask task,
            String parameterName
    ) {
        dynamicList = new HashMap<>();// обнулим лист
        if (task == null || parameterName == null) return;
        Collection<IParameterSelectionChoice> selectionList =
                (Collection<IParameterSelectionChoice>) task.getSelectionList(parameterName);
        if (selectionList != null) {
            for (IParameterSelectionChoice selectionItem : selectionList) {
                dynamicList.put(
                        selectionItem.getValue(),
                        selectionItem.getLabel()
                );
            }
        }
    }
}
