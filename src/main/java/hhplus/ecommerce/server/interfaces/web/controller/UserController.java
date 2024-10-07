package hhplus.ecommerce.server.interfaces.web.controller;

import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RequestMapping("/api/open")
@RestController
public class UserController {

    @GetMapping("/{id}")
    public Map<Object, Object> doSomething(@PathVariable String id,
                                           @RequestParam(required = false) String name) {
        Map<Object, Object> response = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        data.put("name", "John Doe");
        data.put("age", 30);
        data.put("isUser", true);
        response.put("data", data);
        response.put("code", "200");
        response.put("message", "ok");
        return response;
    }

    /*
    Authorization: 없음
    Path Variable : id
    Request Parameter : name

    Response 는 아래쪽의 json
    {
        "code": "200",
        "message": "ok",
        "data": {
            "name": "John Doe",
            "age": 30,
            "isUser": true
        }
    }
     */
}
