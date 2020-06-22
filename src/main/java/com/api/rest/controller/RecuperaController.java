package com.api.rest.controller;

import com.api.rest.ObjetoError;
import com.api.rest.model.User;
import com.api.rest.repository.UserRepository;
import com.api.rest.service.ServiceEnviaEmail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

@RestController
@RequestMapping(value = "/recuperar")
public class RecuperaController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ServiceEnviaEmail serviceEnviaEmail;

    @ResponseBody
    @PostMapping(value = "/")
    public ResponseEntity<ObjetoError> recuperar(@RequestBody User login) throws MessagingException {

        ObjetoError objetoError = new ObjetoError();

        User user = userRepository.findUserByLogin(login.getLogin());

        if (user == null) {
            objetoError.setCode("404"); /* não encontrado */
            objetoError.setError("Usuário não encontrado");
        } else {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String senhaNova = dateFormat.format(Calendar.getInstance().getTime());

            String senhaCriptografada = new BCryptPasswordEncoder().encode(senhaNova);
            userRepository.updatePassword(senhaCriptografada, user.getId());

            serviceEnviaEmail.enviarEmail("Recuperação de senha",
                    user.getEmail(),
                    "Sua nova senha é " + senhaNova);

            objetoError.setCode("200"); /* encontrado */
            objetoError.setError("Acesso enviado para o seu e-mail");
        }

        return new ResponseEntity<ObjetoError>(objetoError, HttpStatus.OK);
    }
}
