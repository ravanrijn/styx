package com.github.kratos.controller

import com.github.kratos.http.UaaClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody

@Controller
@RequestMapping("/api/access_token")
class AccessTokenController {

    final UaaClient uaaClient;

    @Autowired
    def AccessTokenController(UaaClient uaaClient) {
        this.uaaClient = uaaClient
    }

    @RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    def authenticate(@RequestParam("username") username,  @RequestParam("password") password) {
        uaaClient.userToken(username, password)
    }

}
