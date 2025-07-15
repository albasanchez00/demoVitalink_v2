package com.ceatformacion.demovitalink_v2;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfiguration {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        //Configurar las páginas que según el rol mostrará o negará
        http.authorizeHttpRequests(auth->auth.requestMatchers(HttpMethod.GET,"/","/index","/registroUsuario","/media/**","/css/**","/js/**").permitAll()
                //Acceso al crud
                .requestMatchers(HttpMethod.GET,"/panelUsuario").hasAnyRole("Admin","User")

                //Formulario de Gestión de Usuarios: solo rol 'admin'
                .requestMatchers(HttpMethod.GET,"/registroUsuario").permitAll()
                .requestMatchers(HttpMethod.POST,"/guardarUsuario").permitAll()
                .requestMatchers(HttpMethod.GET,"/listaUsuario/{id}").hasRole("Admin")
                .requestMatchers(HttpMethod.POST,"/eliminarUsuario/{id}").hasRole("Admin")
                .requestMatchers(HttpMethod.POST,"/editarUsuario/{id}").hasAnyRole("Admin","User")

                //Formulario de Gestión de Clientes: solo rol 'User'
                .requestMatchers(HttpMethod.GET,"/registroCliente").hasRole("Admin")
                .requestMatchers(HttpMethod.POST,"/guardarCliente").hasRole("Admin")
                .requestMatchers(HttpMethod.GET,"/listaCliente/{id}").hasRole("Admin")
                .requestMatchers(HttpMethod.GET,"/pedirCita").hasRole("User")
                .requestMatchers(HttpMethod.POST,"/guardarCitas").hasRole("User")
                .requestMatchers(HttpMethod.POST,"/tratamientos/**").hasRole("User")
                .requestMatchers(HttpMethod.POST,"/tratamientos/**").hasRole("User")
                //Cualquier otra ruta necesita autentificación.
                .anyRequest().authenticated()
        ).formLogin(form->form.loginPage("/inicioSesion")
                .loginProcessingUrl("/inicioSesion")
                .defaultSuccessUrl("/panelUsuario",true).permitAll()
        ).logout(LogoutConfigurer::permitAll);

        return http.build();
    }

    //Encripta y lee las contraseñas... con BCrypt para usarlo en el login de Spring
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    // Expone un objeto de Spring que usa internamente para autenticar usuarios,
    // y lo hace accesible para que el programador lo pueda utilizar también...
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception{
        return authConfig.getAuthenticationManager();
    }

}
