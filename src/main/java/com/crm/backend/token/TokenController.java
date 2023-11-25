package com.crm.backend.token;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


/**
 * Serves as a controller for requests associated with manipulation over token's data.
 *
 * @author shohrukhyakhyoev
 */
@RestController
@RequestMapping("/api/v1/token")
public class TokenController {

    private final TokenService service;

    @Autowired
    public TokenController(TokenService service) {
        this.service = service;
    }

    @GetMapping("/")
    public List<Token> getTokens() {
        return service.getTokens();
    }

    @GetMapping("/user/")
    public List<Token> getUserTokens(@RequestParam(name="user_id") Long user_id) {
        return service.getUserTokens(user_id);
    }


}
