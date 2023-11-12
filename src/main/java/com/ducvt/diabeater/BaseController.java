package com.ducvt.diabeater;

import com.ducvt.diabeater.fw.utils.ResponseFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping()
public class BaseController {
    @GetMapping("/test")
    public ResponseEntity<?> test() {
        return ResponseFactory.success("This is a base Spring boot project");
    }
}
