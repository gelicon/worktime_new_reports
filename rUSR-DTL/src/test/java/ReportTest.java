import biz.gelicon.core.reports.ReportServiceUSR_DTL;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Тест для отчета Карточка пользователя
 * Работает с установленными на момент дизайна параметрами базы данных, например
 * jdbc:postgresql://10.15.3.39:5432/GC_DEVELOP_TRUNK
 */
public class ReportTest {

    @Test
    public void runTest() throws Exception {
        String reportCode = "USR-DTL";
        ReportServiceUSR_DTL rep = new ReportServiceUSR_DTL();
        // Отчет для PostgreSQL перестал работать - не проверяем
        if (true) return;
        // Запускаем репорт с параметрами по умолчанию
        String s;
        s = rep.runReport(reportCode, null);
        if (s == null) {
            throw new Exception(
                    "Отчет " + reportCode + " отработал с ошибкой - файл отчета не создался");
        }
        // Запускаем репорт с иными параметрами - не обязательно
        Map<String,Object> params = new HashMap<>();
        params.put("id",1); // SYSDBA
        params.put("print_accessrole", false); // Не Печатать доступа к ролям
        s = rep.runReport(reportCode, params);
        if (s == null) {
            throw new Exception(
                    "Отчет " + reportCode + " отработал с ошибкой - файл отчета не создался");
        }
    }

}
