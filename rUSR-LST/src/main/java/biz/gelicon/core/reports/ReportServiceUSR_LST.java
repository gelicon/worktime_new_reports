package biz.gelicon.core.reports;

import biz.gelicon.annotation.ReportProvider;
import biz.gelicon.core.reports.engine.BirtReportEngine;
import biz.gelicon.core.reports.engine.CapitalConst;
import biz.gelicon.core.reports.engine.OutputFormat;
import biz.gelicon.services.ReportDescription;
import biz.gelicon.services.ReportManager;
import biz.gelicon.services.ReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;


/**
 * Регистрация отчета Список пользователей
 */
@ReportProvider
public class ReportServiceUSR_LST implements ReportService {
    private static final Logger logger = LoggerFactory.getLogger(ReportServiceUSR_LST.class);
    private static final String REPORT_CODE = "USR-LST";
    private static final String REPORT_NAME = "Список пользователей";
    private static final String REPORT_UNIT = "credential";
    private static final String REPORT_ENTITY = "proguser";

    @Override
    public void registerReport(ReportManager reportManager) {
        ReportDescription report = reportManager.registerReport(REPORT_CODE, REPORT_NAME);
        report.forUnit(REPORT_UNIT);
        // Декларируем параметр отчета
        report.declareParam(
                "status", // Имя параметра
                "Статус пользователя", // Метка
                ReportDescription.ParamType.Integer, // Тип параметра
                ReportDescription.ParamSubType.CapCode // Подтип - выбор из таблицы capcode
        );
        report.declareOptionsForParam( // Опции параметра
                "status",
                reportManager.createOptionCapCode(
                        CapitalConst.USER_STATUS_TYPE, // capcodetype_id - Тип статуса пользователя
                        true, // Возможно не вводить
                        true  // Кеширование
                )
        );
        /* устарел
        report.declareParam(
                "type",
                "Тип пользователя",
                ReportDescription.ParamType.Integer,
                ReportDescription.ParamSubType.Select // Список выбора
        );
        List<OptionForSelectParam> values = new ArrayList<>();
        values.add(new OptionForSelectParamImpl(-1, "Все"));
        values.add(new OptionForSelectParamImpl( 0, "Пользователи"));
        values.add(new OptionForSelectParamImpl( 1, "Администраторы"));
        report.declareOptionsForParam(
                "type",
                reportManager.createOptionSelect(
                        values,
                        false // nullable
                )
        );
        report.paramInitValue("type", -1);
         */
    }

    @Override
    public String runReport(String code, Map<String, Object> map) {
        logger.info("code {}", code);
        if (map == null) {map = new HashMap<>();}
        // Обязательно надо установить все параметры в по умолчанию, если они не переданы
        Map<String, Object> paraReports = new HashMap<>(); // Карта для параметров в отчет - не равна входным параметрам
        Integer status = (Integer) map.get("status");
        if (status == null) status = 0;  // Все
        paraReports.put("status", status);

        /* Устарел
        Integer type = -1; // Все
        if (map.containsKey("type")) {
            Object o = map.get("type");
            // Защита от всего
            if (o instanceof LinkedHashMap) {
                LinkedHashMap l = (LinkedHashMap) o;
                String s = (String) l.get("value");
                type = Integer.valueOf(s);
            } else if (o instanceof Integer) {
                type = (Integer) o;
            } else if (o instanceof String) {
                type = Integer.valueOf((String) o);
            }
       }
        paraReports.put("type", type);
         */
        logger.info("Параметры {}", paraReports);
        BirtReportEngine engine = BirtReportEngine.newInstance();
        logger.info("executeReport...");
        return engine.executeReport(
                "reports/" + REPORT_CODE + ".rptdesign",
                paraReports,
                OutputFormat.PDF
        );
    }
}
