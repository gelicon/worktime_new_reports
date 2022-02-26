package biz.gelicon.core.reports;

import biz.gelicon.annotation.ReportProvider;
import biz.gelicon.core.reports.engine.BirtReportEngine;
import biz.gelicon.core.reports.engine.OptionForSelectParamImpl;
import biz.gelicon.core.reports.engine.OutputFormat;
import biz.gelicon.services.OptionForSelectParam;
import biz.gelicon.services.ReportDescription;
import biz.gelicon.services.ReportManager;
import biz.gelicon.services.ReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Регистрация отчета Список пользователей
 */
@ReportProvider
public class ReportServiceUSR_DTL implements ReportService {

    private static final Logger logger = LoggerFactory.getLogger(ReportServiceUSR_DTL.class);
    private static final String REPORT_CODE = "USR-DTL";
    private static final String REPORT_NAME = "Карточка пользователя";
    private static final String REPORT_UNIT = "credential";
    private static final String REPORT_ENTITY = "proguser";

    @Override
    public void registerReport(ReportManager reportManager) {
        ReportDescription report = reportManager.registerReport(REPORT_CODE, REPORT_NAME);
        //report.forUnit(REPORT_UNIT);
        report.forEntity(REPORT_ENTITY);
        // Регистрация параметров

        report.declareParam(
                "print_accessrole",
                "Доступ к ролям",
                ReportDescription.ParamType.Boolean
        );
        report.paramInitValue("print_accessrole", true);
        report.declareOptionsForParam(
                "print_accessrole",
                reportManager.createOption(false) // Обязательность
        );

        // Параметр - запрос через url - надо проверять
        if (false) {
            report.declareParam(
                    "id",
                    "Пользователь",
                    ReportDescription.ParamType.Integer,
                    ReportDescription.ParamSubType.Select
            );
            report.declareOptionsForParam(
                    "id",
                    reportManager.createOptionSelect(
                            "admin/credential/proguser/getlist",
                            "{\"pagination\":{\"current\":1, \"pageSize\":50000}, \"sort\":[{\"field\":\"proguserName\", \"order\":\"ascend\"}]}",
                            "proguserId",
                            "proguserName",
                            false, // nullable
                            true //
                    )
            );
        }
    }

    @Override
    public String runReport(String code, Map<String, Object> map) {
        logger.info("code {}", code);
        if (map == null) {map = new HashMap<>();}
        // Обязательно надо установить все параметры в по умолчанию, если они не переданы
        Map<String, Object> paraReports = new HashMap<>(); // Карта для параметров в отчет - не равна входным параметрам
        // id - параметр для основного запроса
        Integer id = (Integer) map.get("proguserId"); // Это должны получить с фронта
        if (id == null) {id = (Integer) map.get("id");}
        if (id == null) {id = 1;} // Пусть печатается для SYSDBA по умолчанию
        paraReports.put("id", id);
        // print_accessrole - печатать доступ на роли
        Boolean printAccessrole = true;
        if (map.containsKey("print_accessrole")) {
            printAccessrole = (Boolean) map.get("print_accessrole");
        }
        paraReports.put("print_accessrole", printAccessrole);
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
