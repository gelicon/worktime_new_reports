package biz.gelicon.core.reports.engine;

import org.eclipse.birt.core.exception.BirtException;
import org.eclipse.birt.core.framework.Platform;
import org.eclipse.birt.report.engine.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * Движок для встроенных отчетов BIRT
 */
public class BirtReportEngine {

    private static final Logger logger = LoggerFactory.getLogger(BirtReportEngine.class);
    private static IReportEngine engine;
    public final static String OUTPUT_PATH =
            System.getProperty("java.io.tmpdir") + "gelicon-report-output" + File.separator;
    // На время разработки установить true
    boolean debugFlag = false;

    private static void prepareReportEngine() throws BirtException, MalformedURLException {
        if (engine == null) {
            EngineConfig config = new EngineConfig();
            config.setLogConfig(System.getProperty("java.io.tmpdir") + "log-gelicon-core-birt",
                    Level.WARNING);
            // Дополнительный файл концигурации шрифтов
            // Принципы работы описаны в нем самом
            // Лежит в lib
            // Не использовать без крайней необходимости.
            // Проще выложить шрифты в папку /usr/share/fonts/TTF без вложенностей
            // и зарегистрировать в Linux командой fc-cache -f -v
            // Не использовать
            //URL url = new URL("file:///usr/share/fonts/gelicon/fontsConfigGelicon.xml");
            //config.setFontConfig(url);
            Platform.startup(config);
            final IReportEngineFactory factory = (IReportEngineFactory) Platform
                    .createFactoryObject(IReportEngineFactory.EXTENSION_REPORT_ENGINE_FACTORY);
            engine = factory.createReportEngine(config);
        }
    }

    protected BirtReportEngine() {
    }

    public static BirtReportEngine newInstance() {
        try {
            prepareReportEngine();
            return new BirtReportEngine();
        } catch (BirtException | MalformedURLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public String executeReport(
            String resoureName,
            Map<String, Object> params,
            OutputFormat format
    ) {
        if (debugFlag)  logger.info("Report {} executing ...", resoureName);
        // загрузка из ресурса
        IReportRunnable design;
        try (InputStream inputStream = getClass().getClassLoader()
                .getResourceAsStream(resoureName)) {
            design = engine.openReportDesign(inputStream);
            if (debugFlag)  logger.info("design ... Ok");
            IRunAndRenderTask task = engine.createRunAndRenderTask(design);
            // Получим параметры из репорта
            List<BirtParamMetadata> birtParamMetadataList = getBirtParamMetadataList(
                    engine,
                    design
            );
            if (debugFlag)  logger.info("birtParamMetadataList ... Ok");
            try {
                // Передадим параметры в отчет
                params.forEach((name, value) -> {
                    // Найдем в репорте
                    BirtParamMetadata birtParamMetadata = birtParamMetadataList.stream()
                            .filter(p -> p.getParameterName().equals(name))
                            .findAny()
                            .orElse(null);
                    if (birtParamMetadata == null) { // Не нашли
                        logger.warn("Параметр {} не найден в отчете", name);
                        return; // нечего устанавливать
                    }
                    task.setParameterValue(name, value); // Установим параметр
                    // Получим и установим из списка изображаемое значение, если список есть
                    if (!birtParamMetadata.getDynamicList().isEmpty()) {
                        String displayText = birtParamMetadata.getDynamicList().get(value);
                        task.setParameterDisplayText(name, displayText);
                    }
                });
                if (debugFlag)
                    logger.info("Parameters ... Ok");
                // Опции
                RenderOption options = getRenderOption(format,
                        File.createTempFile("report", "").getName());
                task.setRenderOption(options);
                //task.setErrorHandlingOption(1);
                if (debugFlag)
                    logger.info("Task running ... ");
                //task.getAppContext().put("OdaJDBCDriverPassInConnection",null);
                task.run();
                // Проверим ошибки
                List tl = task.getErrors();
                if (tl != null && !tl.isEmpty()) {
                    Object err = tl.get(0);
                    if (err instanceof  EngineException) {
                        throw new RuntimeException(((EngineException) err).getMessage(), (Throwable) err);
                    } else if (err instanceof Exception) {
                        throw new RuntimeException(((Exception) err).getMessage(), (Throwable) err);
                    } else {
                        throw new RuntimeException("Неизвестная ошибка за пределами РФ. Класс ошибки '" + err.getClass().getName() + "'");
                    }
                }
                if (debugFlag) { logger.info("Task running ... Ok"); }
                String fName = options.getOutputFileName();
                if (debugFlag)
                    logger.info("OutputFileName = {}", fName);
                return fName;
            } finally {
                task.close();
                if (debugFlag)  logger.info("Task closed");
            }
        } catch (IOException | EngineException ex) {
            throw new RuntimeException(ex);
        }

    }

    public void shutdown() {
        if (engine != null) {
            engine.destroy();
            engine = null;
            Platform.shutdown();
        }
    }

    private RenderOption getRenderOption(OutputFormat format, String outPutFileBase) {
        switch (format) {
            case PDF:
                final PDFRenderOption pdfoptions = new PDFRenderOption();
                pdfoptions.setOutputFileName(OUTPUT_PATH + outPutFileBase + ".pdf");
                pdfoptions.setOutputFormat("pdf");
                pdfoptions.setEmbededFont(true);
                return pdfoptions;
            case HTML:
                final HTMLRenderOption htmpOptions = new HTMLRenderOption();
                htmpOptions.setOutputFileName(OUTPUT_PATH + outPutFileBase + ".html");
                htmpOptions.setOutputFormat("html");
                return htmpOptions;
            case EXCEL:
                final EXCELRenderOption excelRenderOption = new EXCELRenderOption();
                excelRenderOption.setOutputFileName(OUTPUT_PATH + outPutFileBase + ".xlsx");
                excelRenderOption.setOutputFormat("xlsx");
                return excelRenderOption;
            default:
                throw new RuntimeException("Unknown report format");
        }
    }

    /**
     * Возвращает тип визуальной вбивалки параметра в виде строки сделано на будущее
     *
     * @return
     */
    private String getParameterControlType(
            IScalarParameterDefn scalar
    ) {
        switch (scalar.getControlType()) {
            case IScalarParameterDefn.TEXT_BOX:
                return "Text Box";
            case IScalarParameterDefn.LIST_BOX:
                return "List Box";
            case IScalarParameterDefn.RADIO_BUTTON:
                return "Radio Button";
            case IScalarParameterDefn.CHECK_BOX:
                return "Check Box";
            default:
                return "unknown";
        }
    }

    /**
     * Возвращает тип параметра сделано на будущее
     *
     * @return
     */
    private String getParameterDataType(
            IScalarParameterDefn scalar
    ) {
        switch (scalar.getDataType()) {
            case IScalarParameterDefn.TYPE_STRING:
                return "String";
            case IScalarParameterDefn.TYPE_FLOAT:
                return "Float";
            case IScalarParameterDefn.TYPE_DECIMAL:
                return "Decimal";
            case IScalarParameterDefn.TYPE_DATE_TIME:
                return "Date Time";
            case IScalarParameterDefn.TYPE_BOOLEAN:
                return "Boolean";
            case IScalarParameterDefn.TYPE_INTEGER:
                return "Integer";
            case IScalarParameterDefn.TYPE_DATE:
                return "Date";
            case IScalarParameterDefn.TYPE_TIME:
                return "Time";
            case IScalarParameterDefn.TYPE_ANY:
                return "Any";
            default:
                return "Unknown";
        }
    }

    /**
     * Возвращает коллекицю описаний параметров из отчета
     *
     * @param eng движок birt
     * @param des дизайн отчета
     * @return Коллекция описаний параметров отчета
     */
    public List<BirtParamMetadata> getBirtParamMetadataList(
            IReportEngine eng,
            IReportRunnable des
    ) {
        // Эту ссылку не убирать!
        // http://git.srv.gelicon.biz/gelicon-core/backend/src/birt-test/src/main/java/biz/gelicon/capital/report/ParametersTask.java#L100
        List<BirtParamMetadata> birtParamMetadataList = new ArrayList<>();
        // Задача
        IGetParameterDefinitionTask task = eng.createGetParameterDefinitionTask(des);
        // Коллекция описания параметров
        Collection<IParameterDefnBase> parameterDefnBaseCollection =
                (Collection<IParameterDefnBase>) task.getParameterDefns(true);
        // Для каждого параметра
        parameterDefnBaseCollection.forEach(p -> {
            // Скаляр
            IScalarParameterDefn scalar = (IScalarParameterDefn) p;
            String name = scalar.getName();
            BirtParamMetadata birtParamMetadata = new BirtParamMetadata(name); // Имя
            birtParamMetadata.setParameterControlType(scalar.getControlType()); // Тип вбивалки
            birtParamMetadata.setParameterDataType(scalar.getDataType()); // Тип значения
            birtParamMetadata.setDefaultValue(scalar.getDefaultValue());// Значение по умолчанию
            // Получение списка значений
            birtParamMetadata.buildDynamicList(task, name);
            birtParamMetadataList.add(birtParamMetadata);// Добавляем
        });
        return birtParamMetadataList;
    }


}
