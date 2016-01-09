package org.igov.service.controller;

import com.google.common.base.Optional;
import io.swagger.annotations.*;
import org.activiti.engine.impl.util.json.JSONObject;
import org.slf4j.Logger;import org.slf4j.LoggerFactory;
import org.igov.activiti.bp.HistoryEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.igov.model.HistoryEvent_ServiceDao;
import org.igov.model.ServerDao;
import org.igov.io.liqpay.LiqBuyUtil;
import org.igov.model.HistoryEvent_Service;
import org.igov.model.Server;
import org.igov.io.web.HttpRequester;
import org.igov.io.GeneralConfig;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import org.igov.service.interceptor.exception.ActivitiRestException;
import org.igov.service.interceptor.exception.RecordNotFoundException;

@Controller
@Api(tags = { "ActionTaskCentralController" }, description = "Действия задачи центрально")
@RequestMapping(value = "/action/task")
public class ActionTaskCentralController {

    private static final Logger LOG = LoggerFactory.getLogger(ActionTaskCentralController.class);

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    // Подробные описания сервисов для документирования в Swagger
    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    private static final String noteCODE = "\n```\n";
    private static final String noteCODEJSON = "\n```json\n";
    private static final String noteController = "##### ActionTaskCentralController - Действия задачи центрально";

    private static final String noteSetTaskAnswer_Central = noteController + "Установить ответ по завечанию на таску #####\n\n";

    private static final String noteGetStartFormByTask =
            noteController + "Получение полей стартовой формы по: ИД субьекта, ИД услуги, ИД места Услуги #####\n\n"
                    + "HTTP Context: https://test.igov.org.ua/wf-central/service/action/task/getStartFormByTask_Central?nID_Subject=nID_Subject&nID_Service=nID_Service&sID_UA=sID_UA&nID_Server=nID_Server\n\n"
                    + "возвращает JSON содержащий поля стартовой формы процесса, процесс находится на основании ИД таски полученой из сущности HistoryEvent_Service. На основании HistoryEvent_Service определяется региональный сервер откуда нужно получить поля формы и собственно ИД таски.\n\n"
                    + "- nID_Subject - номер-ИД субьекта (переменная обязательна)\n"
                    + "- nID_Service - номер-ИД услуги (переменная обязательна)\n"
                    + "- sID_UA - строка-ИД места Услуги (переменная обязательна)\n"
                    + "- nID_Server - номер-ИД сервера опциональный, по умолчанию 0\n\n\n"
                    + "Примеры:\n"
                    + "https://test.igov.org.ua/wf-central/service/action/task/getStartFormByTask_Central?nID_Subject=2&nID_Service=1&sID_UA=1200000000\n\n"
                    + "Ответ, если запись существует (HTTP status Code: 200 OK):\n"
                    + noteCODEJSON
                    + "{\n"
                    + "  waterback=\"--------------------\",\n"
                    + "  phone=\"380979362996\",\n"
                    + "  date_from=\"01/01/2014\",\n"
                    + "  bankIdbirthDay=\"27.05.1985\",\n"
                    + "  notice2=\"Я та особи, які зареєстровані (фактично проживають) у житловому приміщенні/будинку, даємо згоду на обробку персональних даних про сім’ю, доходи, майно, що необхідні для призначення житлової субсидії, та оприлюднення відомостей щодо її призначення.\",\n"
                    + "  house=\"--------------------\",\n"
                    + "garbage=\"--------------------\",\n"
                    + "  waterback_notice=\"\",\n"
                    + "  garbage_number=\"\",\n"
                    + "  floors=\"10\",\n"
                    + "  name_services=\"--------------------\",\n"
                    + "  date_to=\"30/12/2014\",\n"
                    + "  date3=\"\",\n"
                    + "  date2=\"\",\n"
                    + "  electricity=\"--------------------\",\n"
                    + "  garbage_name=\"\",\n"
                    + "  date1=\"\",\n"
                    + "  place_type=\"2\",\n"
                    + "  bankIdfirstName=\"ДМИТРО\",\n"
                    + "  declaration=\"--------------------\",\n"
                    + "  waterback_name=\"\",\n"
                    + "  electricity_notice=\"\",\n"
                    + "  bankIdinn=\"3119325858\",\n"
                    + "  house_name=\"\",\n"
                    + "  gas=\"--------------------\",\n"
                    + "  house_number=\"\",\n"
                    + "  subsidy=\"1\",\n"
                    + "  email=\"dmitrij.zabrudskij@privatbank.ua\",\n"
                    + "  warming=\"--------------------\",\n"
                    + "  hotwater_notice=\"\",\n"
                    + "  org0=\"Назва організації\",\n"
                    + "  org1=\"\",\n"
                    + "  electricity_number=\"123456\",\n"
                    + "  org2=\"\",\n"
                    + "  org3=\"\",\n"
                    + "  warming_name=\"\",\n"
                    + "  place_of_living=\"Дніпропетровська, Дніпропетровськ, пр. Героїв, 17, кв 120\",\n"
                    + "  fio2=\"\",\n"
                    + "  fio3=\"\",\n"
                    + "  total_place=\"68\",\n"
                    + "  garbage_notice=\"\",\n"
                    + "  fio1=\"\",\n"
                    + "  chapter1=\"--------------------\",\n"
                    + "  bankIdmiddleName=\"ОЛЕКСАНДРОВИЧ\",\n"
                    + "  gas_name=\"\",\n"
                    + "  bankIdPassport=\"АМ765369 ЖОВТНЕВИМ РВ ДМУ УМВС УКРАЇНИ В ДНІПРОПЕТРОВСЬКІЙ ОБЛАСТІ 18.03.2002\",\n"
                    + "  warming_place=\"45\",\n"
                    + "  passport3=\"\",\n"
                    + "  gas_number=\"\",\n"
                    + "  passport2=\"\",\n"
                    + "  electricity_name=\"коммуна\",\n"
                    + "  area=\"samar\",\n"
                    + "  house_notice=\"\",\n"
                    + "  bankIdlastName=\"ДУБІЛЕТ\",\n"
                    + "  card1=\"\",\n"
                    + "  card3=\"\",\n"
                    + "  coolwater_number=\"\",\n"
                    + "  card2=\"\",\n"
                    + "  warming_notice=\"\",\n"
                    + "  hotwater_name=\"\",\n"
                    + "  income0=\"attr9\",\n"
                    + "  coolwater=\"--------------------\",\n"
                    + "  gas_notice=\"\",\n"
                    + "  overload=\"hxhxfhfxhfghg\",\n"
                    + "  warming_number=\"\",\n"
                    + "  income3=\"attr0\",\n"
                    + "  income1=\"attr0\",\n"
                    + "  income2=\"attr0\",\n"
                    + "  passport1=\"\",\n"
                    + "  coolwater_notice=\"\",\n"
                    + "  sBody_1=\"null\",\n"
                    + "  hotwater=\"--------------------\",\n"
                    + "  coolwater_name=\"\",\n"
                    + "  waterback_number=\"\",\n"
                    + "  man1=\"\",\n"
                    + "  hotwater_number=\"\",\n"
                    + "  sBody_2=\"null\",\n"
                    + "  comment=\"null\",\n"
                    + "  decision=\"null\",\n"
                    + "  selection=\"attr1\"\n"
                    + "}\n\n\n"
                    + noteCODE
                    + "Ответ, если записи не существует. (HTTP status Code: 500 Internal Server Error):\n"
                    + noteCODEJSON
                    + "{\n"
                    + "  \"code\": \"BUSINESS_ERR\",\n"
                    + "  \"message\": \"Record not found\"\n"
                    + "}"
                    + noteCODE;    
    
    @Autowired
    HttpRequester httpRequester;

    @Autowired
    private HistoryEvent_ServiceDao historyEventServiceDao;
    //@Autowired
    //private HistoryEventDao historyEventDao;

    //@Autowired
    //@Qualifier("regionDao")
    //private GenericEntityDao<Region> regionDao;

    @Autowired
    private ServerDao serverDao;
    @Autowired
    private GeneralConfig generalConfig;

    @Autowired
    private HistoryEventService historyEventService;
    //@Autowired
    //private ServerDao serverDao;
    //@Autowired
    //HttpRequester httpRequester;

    /**
     * @param nID_Protected номер-ИД заявки (защищенный, опционально, если есть sID_Order или nID_Process)
     * @param sID_Order     строка-ид заявки (опционально, подробнее [тут](https://github.com/e-government-ua/i/blob/test/docs/specification.md#17_workWithHistoryEvent_Services) )
     * @param nID_Process   ид заявки (опционально)
     * @param nID_Server    ид сервера, где расположена заявка
     * @param saField       строка-массива полей (например: "[{'id':'sFamily','type':'string','value':'Белявцев'},{'id':'nAge','type':'long','value':35}]")
     * @param sToken        строка-токена. Данный параметр формируется и сохраняется в запись HistoryEvent_Service во время вызова метода setTaskQuestions
     * @param sHead         строка заголовка сообщения (опциональный параметр)
     * @param sBody         строка тела сообщения (опциональный параметр)
     */
    @ApiOperation(value = "/setTaskAnswer_Central", notes = noteSetTaskAnswer_Central)
    @RequestMapping(value = "/setTaskAnswer_Central", method = RequestMethod.GET)
    public
    @ResponseBody
    void setTaskAnswer(
            @ApiParam(value = "строка-ид заявки", required = false) @RequestParam(value = "sID_Order", required = false) String sID_Order,
            @ApiParam(value = "номер-ИД заявки (защищенный, опционально, если есть sID_Order или nID_Process)", required = false) @RequestParam(value = "nID_Protected", required = false) Long nID_Protected,
            @ApiParam(value = "ид заявки", required = false) @RequestParam(value = "nID_Process", required = false) Long nID_Process,
            @ApiParam(value = "ид сервера", required = false) @RequestParam(value = "nID_Server", required = false) Integer nID_Server,
            @ApiParam(value = "строка-массива полей (например: \"[{'id':'sFamily','type':'string','value':'Белявцев'},{'id':'nAge','type':'long','value':35}]\")", required = true) @RequestParam(value = "saField") String saField,
            @ApiParam(value = "строка-токена. Данный параметр формируется и сохраняется в запись HistoryEvent_Service во время вызова метода setTaskQuestions", required = true) @RequestParam(value = "sToken") String sToken,
            @ApiParam(value = "строка заголовка сообщения", required = false) @RequestParam(value = "sHead", required = false) String sHead,
            @ApiParam(value = "строка тела сообщения", required = false) @RequestParam(value = "sBody", required = false) String sBody)
            throws ActivitiRestException {

        try {
            LOG.info(
                    "try to find history event_service by sID_Order=" + sID_Order + ", nID_Protected-" + nID_Protected
                            + ", nID_Process=" + nID_Process + " and nID_Server=" + nID_Server
            );
            String historyEvent = historyEventService.getHistoryEvent(
                    sID_Order, nID_Protected, nID_Process, nID_Server);
            LOG.info("....ok! successfully get historyEvent_service! event="
                    + historyEvent);

            JSONObject fieldsJson = new JSONObject(historyEvent);
            String processInstanceID = fieldsJson.get("nID_Task").toString();
            sHead = sHead != null ? sHead : "На заявку "
                    + fieldsJson.getString("sID_Order")
                    + " дана відповідь громаданином";
            if (fieldsJson.has("sToken")) {
                String tasksToken = fieldsJson.getString("sToken");
                if (tasksToken.isEmpty() || !tasksToken.equals(sToken)) {
                    throw new ActivitiRestException(
                            ExceptionCommonController.BUSINESS_ERROR_CODE,
                            "Token is wrong");
                }
            } else {
                throw new ActivitiRestException(
                        ExceptionCommonController.BUSINESS_ERROR_CODE,
                        "Token is absent");
            }

            String sHost = null;
            Optional<Server> oOptionalServer = serverDao.findById(Long.valueOf(nID_Server + ""));
            if (!oOptionalServer.isPresent()) {
                throw new RecordNotFoundException();
            } else {//https://test.region.igov.org.ua/wf
                //sHost = oOptionalServer.get().getsURL_Alpha();
                sHost = oOptionalServer.get().getsURL();
            }

            String sURL = sHost + "/service/action/task/setTaskAnswer";
            LOG.info("sURL=" + sURL);

            Map<String, String> mParam = new HashMap<String, String>();
            mParam.put("nID_Process", processInstanceID);//nID_Process
            mParam.put("saField", saField);
            mParam.put("sBody", sBody);
            LOG.info("mParam=" + mParam);
            String sReturn = httpRequester.get(sURL, mParam);
            LOG.info("sReturn=" + sReturn);

            LOG.info(
                    "try to find history event_service by sID_Order=" + sID_Order + ", nID_Protected-" + nID_Protected
                            + " and nID_Server=" + nID_Server
            );

            saField = "[]";
            historyEvent = updateHistoryEvent_Service_Central(sID_Order, nID_Protected,
                    nID_Process, nID_Server, saField, sHead, null, null,
                    "Відповідь на запит по уточненню даних");
            LOG.info("....ok! successfully get historyEvent_service! event="
                    + historyEvent);
        } catch (Exception e) {
            throw new ActivitiRestException(
                    ExceptionCommonController.BUSINESS_ERROR_CODE,
                    e.getMessage(), e, HttpStatus.FORBIDDEN);
        }
    }
    

    public String updateHistoryEvent_Service_Central(String sID_Order,
            Long nID_Protected, Long nID_Process, Integer nID_Server,
            String saField, String sHead, String sBody, String sToken,
            String sID_Status) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("sID_Order", sID_Order);
        params.put("nID_Protected", nID_Protected != null ? "" + nID_Protected
                : null);
        String sID_Process = nID_Process != null ? "" + nID_Process : null;
        params.put("nID_Process", sID_Process);
        params.put("nID_Server", nID_Server != null ? "" + nID_Server : null);
        params.put("soData", saField);
        params.put("sHead", sHead);
        params.put("sBody", sBody);
        params.put("sToken", sToken);
        params.put("sID_Status", sID_Status);
        return historyEventService.updateHistoryEvent(sID_Process, sID_Status,
                true, params);
    }
    
    /**
     * @param nID_Subject номер-ИД субьекта (переменная обязательна)
     * @param nID_Service номер-ИД услуги  (переменная обязательна)
     * @param sID_UA      строка-ИД места Услуги  (переменная обязательна)
     * @param nID_Server  номер-ИД сервера опциональный, по умолчанию 0
     */
    @ApiOperation(value = "Получение полей стартовой формы по: ИД субьекта, ИД услуги, ИД места Услуги", notes = noteGetStartFormByTask)
    @ApiResponses(value = { @ApiResponse(code = 500, message = "Record not found") })
    @RequestMapping(value = "/getStartFormByTask_Central", method = RequestMethod.GET,
            produces = "application/json;charset=UTF-8")
    public
    @ResponseBody
    String getStartFormByTask(
            @ApiParam(value = "номер-ИД субьекта", required = true) @RequestParam(value = "nID_Subject") Long nID_Subject,
            @ApiParam(value = "номер-ИД сервера", required = false) @RequestParam(value = "nID_Server", required = false, defaultValue = "0") Integer nID_Server,
            @ApiParam(value = "номер-ИД услуги", required = true) @RequestParam(value = "nID_Service") Long nID_Service,
            @ApiParam(value = "строка-ИД места Услуги", required = true) @RequestParam(value = "sID_UA") String sID_UA)
            throws RecordNotFoundException {
        String URI = "/service/action/task/getStartFormData?nID_Task=";

        HistoryEvent_Service historyEventService = historyEventServiceDao
                .getLastTaskHistory(nID_Subject, nID_Service,
                        sID_UA);
        if (historyEventService == null) {
            throw new RecordNotFoundException("HistoryEvent_Service wasn't found.");
        }

        Long nID_Task = historyEventService.getnID_Task();
        nID_Server = historyEventService.getnID_Server();
        nID_Server = nID_Server == null ? 0 : nID_Server;

        Optional<Server> serverOpt = serverDao.findById(new Long(nID_Server));
        if (!serverOpt.isPresent()) {
            throw new RecordNotFoundException("Server with nID_Server " + nID_Server + " wasn't found.");
        }
        Server server = serverOpt.get();
        String serverUrl = server.getsURL();
        if (server.getId().equals(0L)) {
            serverUrl = "https://test.region.igov.org.ua/wf";
        }

        serverUrl = serverUrl + URI + nID_Task;

        String sUser = generalConfig.sAuthLogin();
        String sPassword = generalConfig.sAuthPassword();
        String sAuth = LiqBuyUtil.base64_encode(sUser + ":" + sPassword);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Basic " + sAuth);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> httpEntity = new HttpEntity<>(headers);

        RestTemplate template = new RestTemplate();
        template.getMessageConverters().add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));
        LOG.info("Calling URL with parametes " + serverUrl);
        ResponseEntity<String> result;

        try {
            result = template.exchange(serverUrl, HttpMethod.GET, httpEntity, String.class);
        } catch (RestClientException e) {
            LOG.warn(e.getMessage(), e);
            throw new RecordNotFoundException();
        }

        return result.getBody();
    }

}
