import biz.gelicon.core.reports.ReportServiceUSR_LST;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Тест для отчета Список пользователей
 * Работает с установленными на момент дизайна параметрами базы данных, например
 * jdbc:postgresql://10.15.3.39:5432/GC_DEVELOP_TRUNK
 */
public class ReportTest {

    @Test
    public void runTest() throws Exception {
        String reportCode = "USR-LST";
        ReportServiceUSR_LST rep = new ReportServiceUSR_LST();
        String s = rep.runReport(reportCode, null);
        if (s == null) {
            throw new Exception(
                    "Отчет " + reportCode + " отработал с ошибкой - файл отчета не создался");
        }
        // Запускаем репорт с иными параметрами - не обязательно
        Map<String,Object> params = new HashMap<>();
        params.put("status", 1301); // Активный
        params.put("type", 0); // Пользователи
        s = rep.runReport(reportCode, params);
        if (s == null) {
            throw new Exception(
                    "Отчет " + reportCode + " отработал с ошибкой - файл отчета не создался");
        }
    }

}
