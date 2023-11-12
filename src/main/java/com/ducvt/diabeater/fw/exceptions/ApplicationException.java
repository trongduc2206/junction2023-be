package com.ducvt.diabeater.fw.exceptions;

import com.ducvt.diabeater.fw.constant.MessageEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;



@Getter
@AllArgsConstructor
public class ApplicationException extends RuntimeException {
    private final String code;

    // The message is used when you have a detail message which is difference code
    // The message is only used to log, is wont show to end user
    public ApplicationException(String code, String message) {
        super(message);
        this.code = code;
    }

    public ApplicationException(MessageEnum messageEnum) {
        this(messageEnum.getCode(), messageEnum.getMessage());
    }
}
