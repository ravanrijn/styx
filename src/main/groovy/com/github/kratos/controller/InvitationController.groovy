package com.github.kratos.controller

import com.github.kratos.http.ApiClient
import org.apache.commons.codec.binary.Base64
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import sun.misc.BASE64Decoder
import sun.misc.BASE64Encoder

import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.DESKeySpec
import javax.servlet.http.HttpServletRequest

import static org.springframework.http.HttpStatus.CREATED
import static org.springframework.http.HttpStatus.OK
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import static org.springframework.web.bind.annotation.RequestMethod.*

@Controller
@RequestMapping("/api/invitations")
class InvitationController {

    private static final Logger LOG = LoggerFactory.getLogger(InvitationController.class)

    private final JavaMailSender mailSender
    private final ApiClient apiClient
    private final SecretKey key
    private final BASE64Encoder base64Encoder = new BASE64Encoder()
    private final BASE64Decoder base64Decoder = new BASE64Decoder()

    @Autowired
    def InvitationController(JavaMailSender mailSender, ApiClient apiClient, String clientSecret) {
        this.mailSender = mailSender
        this.apiClient = apiClient
        def spec = new DESKeySpec(clientSecret.getBytes("UTF-8"))
        key = SecretKeyFactory.getInstance("DES").generateSecret(spec)
    }

    @RequestMapping(value = "/{invitationKey}", method = GET, produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(OK)
    @ResponseBody
    def register(@PathVariable("invitationKey") String invitationKey) {
        apiClient.inactiveUser(decryptUserId(invitationKey))
    }

    @RequestMapping(value = "/{invitationKey}", method = PUT, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    @ResponseStatus(CREATED)
    def activate(@RequestBody user, @PathVariable("invitationKey") String invitationKey) {
        final userId = decryptUserId(invitationKey);
        LOG.info("Attempting to activate user {}", userId)
        user.id = userId
        apiClient.activateUser(user)
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
                "${httpRequest.scheme}://${httpRequest.serverName}:${httpRequest.serverPort}${httpRequest.contextPath}/#/invitations/${URLEncoder.encode(encryptUserId(inactiveUser.id), "UTF-8")}");
        message.setTo(request.email)
        mailSender.send(message);
        inactiveUser
    }

    def encryptUserId(String userId) {
        final cipher = Cipher.getInstance("DES")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        new String(Base64.encodeBase64URLSafe(cipher.doFinal(userId.getBytes("UTF-8"))), "UTF-8")
    }

    def decryptUserId(String encryptedUserId) {
        final cipher = Cipher.getInstance("DES")
        cipher.init(Cipher.DECRYPT_MODE, key)
        new String(cipher.doFinal(Base64.decodeBase64(encryptedUserId)))
    }

}
