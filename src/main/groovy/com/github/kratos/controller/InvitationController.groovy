package com.github.kratos.controller

import com.github.kratos.http.ApiClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus
import sun.misc.BASE64Decoder
import sun.misc.BASE64Encoder

import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.DESKeySpec
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.servlet.http.HttpServletRequest

import static org.springframework.http.HttpStatus.CREATED
import static org.springframework.http.HttpStatus.OK
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import static org.springframework.http.MediaType.TEXT_HTML_VALUE
import static org.springframework.web.bind.annotation.RequestMethod.GET
import static org.springframework.web.bind.annotation.RequestMethod.POST

@Controller
@RequestMapping("/api/invitations")
class InvitationController {

    private final JavaMailSender mailSender
    private final ApiClient apiClient
    private final SecretKey key
    private final BASE64Encoder base64Encoder = new BASE64Encoder()
    private final BASE64Decoder base64Decoder = new BASE64Decoder()

    @Autowired
    def InvitationController(JavaMailSender mailSender, ApiClient apiClient, String clientSecret){
        this.mailSender = mailSender
        this.apiClient = apiClient
        def spec = new DESKeySpec(clientSecret.getBytes("UTF-8"))
        key = SecretKeyFactory.getInstance("DES").generateSecret(spec)
    }

    @RequestMapping(value = "/{invitationKey}", method = GET, produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(OK)
    @ResponseBody
    def register(@PathVariable("invitationKey") String invitationKey){
        apiClient.inactiveUser(decryptUserId(invitationKey))
    }

    @RequestMapping(method = POST, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    @ResponseStatus(CREATED)
    def invite(@RequestHeader("Authorization") token, @RequestBody request, HttpServletRequest httpRequest) {
        def inactiveUser = apiClient.createInactiveUser(request)
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("eden.cloudfoundry@klm.com");
        message.setSubject("You have been invited to join CloudFoundry!");
        message.setText("You have been invited to join ${inactiveUser.organization.name}\n" +
                "Please follow the link below to finalize your registration.\n\n" +
                "${httpRequest.scheme}://${httpRequest.serverName}:${httpRequest.serverPort}${httpRequest.contextPath}/api/invitations/${URLEncoder.encode(encryptUserId(inactiveUser.id), "UTF-8")}");
        message.setTo(request.email)
        mailSender.send(message);
        inactiveUser
    }

    def encryptUserId(String userId){
        final cipher = Cipher.getInstance("DES")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        base64Encoder.encode(cipher.doFinal(userId.getBytes("UTF-8")))
    }

    def decryptUserId(String encryptedUserId){
        final cipher = Cipher.getInstance("DES")
        cipher.init(Cipher.DECRYPT_MODE, key)
        new String(cipher.doFinal(base64Decoder.decodeBuffer(encryptedUserId)), "UTF-8")
    }

}
