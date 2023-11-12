package com.ducvt.diabeater.fw.utils;


import com.ducvt.diabeater.fw.constant.MessageConstant;
import com.ducvt.diabeater.fw.constant.MessageEnum;
import com.ducvt.diabeater.fw.domain.GeneralResponse;
import com.ducvt.diabeater.fw.domain.ResponseStatus;
import com.ducvt.diabeater.fw.domain.SystemMessage;
import com.ducvt.diabeater.fw.exceptions.BadRequestException;
import org.apache.logging.log4j.util.Strings;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.LinkedHashMap;
import java.util.Map;

public class ResponseFactory {

    private static Map<String, SystemMessage> systemMessageMap = new LinkedHashMap<>();

	private ResponseFactory() {
		throw new IllegalStateException("Utility class");
	}


    public static <T> ResponseEntity<GeneralResponse<T>> success(T data) {
        GeneralResponse<T> generalResponse = new GeneralResponse<>();
        generalResponse.setStatus(getResponseStatus(MessageConstant.SUCCESS));
        generalResponse.setData(data);
        return ResponseEntity.ok(generalResponse);
    }

    public static ResponseEntity success(GeneralResponse responseObject) {
        return ResponseEntity.ok(responseObject);
    }

	public static ResponseEntity success() {
		GeneralResponse<Object> responseObject = new GeneralResponse<>();
		responseObject.setStatus(getResponseStatus(MessageConstant.SUCCESS));
		return ResponseEntity.ok(responseObject);
	}

	public static ResponseEntity success(Object data, Class<?> clazz) {
		GeneralResponse<Object> responseObject = new GeneralResponse<>();
		responseObject.setStatus(getResponseStatus(MessageConstant.SUCCESS));
		responseObject.setData(clazz.cast(data));
		return ResponseEntity.ok(responseObject);
	}

    public static ResponseEntity error(HttpStatus httpStatus, String code) {
        return error(httpStatus, code, null);
    }

    public static ResponseEntity error(HttpStatus httpStatus, String code, Object data) {
        GeneralResponse responseObject = new GeneralResponse();
        responseObject.setStatus(getResponseStatus(code));
        responseObject.setData(data);
        return new ResponseEntity<>(responseObject, httpStatus);
    }

	//Use MessageEnum

    public static ResponseEntity error(HttpStatus httpStatus, BadRequestException e) {
        GeneralResponse responseObject = new GeneralResponse();
        responseObject.setStatus(getResponseStatus(e));
        return new ResponseEntity<>(responseObject, httpStatus);
    }

    private static ResponseStatus getResponseStatus(BadRequestException e) {
	    if (Strings.isBlank(e.getDescription()))
            return new ResponseStatus(e.getMessageEnum().getCode(), e.getMessageEnum().getMessage());
	    return new ResponseStatus(e.getMessageEnum().getCode(), e.getDescription());
    }

	public static ResponseEntity error(HttpStatus httpStatus, MessageEnum responseStatus) {
		GeneralResponse responseObject = new GeneralResponse();
		responseObject.setStatus(getResponseStatus(responseStatus));
		return new ResponseEntity<>(responseObject, httpStatus);
	}

	private static ResponseStatus getResponseStatus(MessageEnum responseStatus) {
		return new ResponseStatus(responseStatus.getCode(), responseStatus.getMessage());
	}

	//Common function

	public static ResponseStatus getResponseStatus(String code) {
        return new ResponseStatus(code, getMessFromCache(code));
    }

	public static String getMessFromCache(String code) {
        if (systemMessageMap == null) {
            return code;
        }
        SystemMessage systemMessage = systemMessageMap.get(code);
        if (systemMessage != null) {
            String mess = systemMessage.getStrMessageVi();
            if (mess != null && !mess.isEmpty()) {
                return mess;
            }
        }
        return code;
    }

    public static void setSysMessMap(Map<String, SystemMessage> systemMessageMap) {
        ResponseFactory.systemMessageMap = systemMessageMap;
    }
}
