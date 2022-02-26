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
import java.util.List;
import java.util.Map;

@ReportProvider
public class ReportService0002 implements ReportService {
    private static final Logger logger = LoggerFactory.getLogger(ReportService0002.class);
    private static final String REPORT_CODE = "0002";
    private static final String REPORT_NAME = "Тестирование параметров";


    @Override
    public void registerReport(ReportManager manager) {
        ReportDescription report = manager.registerReport(REPORT_CODE, REPORT_NAME);
        report.forUnit("edizm");
        report.declareParam("onlyNotBlock", "Только не заблокированные",ReportDescription.ParamType.Boolean);
        report.paramInitValue("onlyNotBlock", true);
        report.declareOptionsForParam("onlyNotBlock",manager.createOption(false));
        report.declareParam("str1", "Test str1",ReportDescription.ParamType.String);
        report.declareOptionsForParam("str1",manager.createOption(false));
        report.declareParam("date1", "Test date1",ReportDescription.ParamType.Date);
        report.declareParam("daterange1", "Test daterange1",ReportDescription.ParamType.DateRange);

        report.declareParam("capcode1", "Test capcode1",ReportDescription.ParamType.Integer,ReportDescription.ParamSubType.CapCode);
        report.declareOptionsForParam("capcode1",manager.createOptionCapCode(13,false,false));
        report.paramInitValue("capcode1",1302,"Это пример установки по умолчанию");

        report.declareParam("select1", "Test select1",ReportDescription.ParamType.Integer,ReportDescription.ParamSubType.Select);
        report.declareOptionsForParam("select1",manager.createOptionSelect("capcode/getlist","{\"capCodeType\":13}",
                "capCodeId","capCodeName",true,true));

        report.declareParam("select2", "Test select2",ReportDescription.ParamType.Integer,ReportDescription.ParamSubType.Select);

        List<OptionForSelectParam> values = new ArrayList<>();
        values.add(new OptionForSelectParamImpl(1, "Все"));
        values.add(new OptionForSelectParamImpl(2, "Только активные"));
        values.add(new OptionForSelectParamImpl(3, "Только пассивные"));
        report.declareOptionsForParam("select2",manager.createOptionSelect(values,false));
        report.paramInitValue("select2",2);

// Параметр - запрос через url
        /*
        report.declareParam(
                "id",
                "Пользователь",
                ReportDescription.ParamType.Integer,
                ReportDescription.ParamSubType.Select
        );
        report.declareOptionsForParam(
                "id",
                manager.createOptionSelect(
                        "admin/credential/proguser/getlist",
                        "", //      Фильтр    ""{\"capCodeType\":13}",
                        "proguserId",
                        "proguserName",
                        false, // nullable
                        true //
                )
        );

         */
    }

    @Override
    public String runReport(String code, Map<String, Object> params) {
        logger.info("Параметры {}",params);
        BirtReportEngine engine = BirtReportEngine.newInstance();
        return engine.executeReport("reports/r0002.rptdesign",params, OutputFormat.PDF);
    }
}
