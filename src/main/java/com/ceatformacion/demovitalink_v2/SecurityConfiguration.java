package com.ceatformacion.demovitalink_v2;

import com.ceatformacion.demovitalink_v2.security.CompositePasswordEncoder;
import com.ceatformacion.demovitalink_v2.services.UsuariosDetailsServices;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity // habilita @PreAuthorize y demás anotaciones a nivel de método
public class SecurityConfiguration {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           AuthenticationProvider authenticationProvider) throws Exception {
        //Configurar las páginas que según el rol mostrará o negará
        http
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))
                .authorizeHttpRequests(auth->auth

                // Públicos + estáticos
                .requestMatchers(HttpMethod.GET,"/","/index",
                        "/politicaPrivacidad", "/terminoCondiciones",
                        "/politicaCookies", "/baseLegal", "/api/**","/webjars/**","/usuarios/**","/usuarios/registroUsuario","/media/**","/css/**","/js/**").permitAll()

                // LOGIN (liberar ambos métodos para evitar el bucle)
                .requestMatchers(HttpMethod.GET,  "/usuarios/inicioSesion").hasAnyRole("Admin","User")
                .requestMatchers(HttpMethod.POST, "/usuarios/inicioSesion").hasAnyRole("Admin","User")
                .requestMatchers(HttpMethod.GET, "/api/sintomas/mios").permitAll() // <- temporal para ver datos
                .requestMatchers("/error").permitAll()

                // Vistas protegidas (usa AUTHORITIES si tus valores son "Admin"/"User")
                .requestMatchers(HttpMethod.GET, "/usuarios/panelUsuario").permitAll()
                .requestMatchers("/usuarios/registroSintomas").permitAll()
                // Registro público
                .requestMatchers(HttpMethod.GET,  "/usuarios/registroUsuario").permitAll()
                .requestMatchers(HttpMethod.POST, "/usuarios/guardarUsuario").permitAll()

                .requestMatchers(HttpMethod.POST, "/usuarios/historialPaciente").permitAll()

                //Acceso al crud
                .requestMatchers(HttpMethod.GET,"/serviciosCliente").permitAll()
                .requestMatchers(HttpMethod.GET,"/serviciosEmpresa").permitAll()
                .requestMatchers(HttpMethod.POST,"/recordatorios").permitAll()
                .requestMatchers(HttpMethod.POST,"/historialPaciente").permitAll()
                .requestMatchers(HttpMethod.POST,"/registroSintomas").permitAll()
                .requestMatchers(HttpMethod.GET,"/serviciosEmpresa").permitAll()
                .requestMatchers(HttpMethod.GET,"/contacto").permitAll()

                //Formulario de Gestión de Usuarios: solo rol 'admin'
                .requestMatchers(HttpMethod.GET,"/listaUsuarios/{id}").hasRole("Admin")
                .requestMatchers(HttpMethod.POST,"/eliminarUsuario/{id}").hasRole("Admin")
                .requestMatchers(HttpMethod.GET, "/citasCliente").hasRole("Admin")
                .requestMatchers(HttpMethod.GET, "/agendaCitas").hasRole("Admin")
                .requestMatchers(HttpMethod.POST,"/editarUsuario/{id}").hasAnyRole("Admin","User")
                .requestMatchers("/admin/**").hasRole("Admin")

                //Formulario de Gestión de Clientes: solo rol 'User'
                .requestMatchers(HttpMethod.GET,"/registroCliente").hasRole("Admin")
                .requestMatchers(HttpMethod.POST,"/guardarCliente").hasRole("Admin")
                .requestMatchers(HttpMethod.GET,"/listaClientes/{id}").hasRole("Admin")
                .requestMatchers(HttpMethod.GET,"/pedirCita").hasRole("User")
                .requestMatchers(HttpMethod.POST,"/guardarCitas").hasRole("User")
                .requestMatchers(HttpMethod.POST,"/tratamientos/**").hasRole("User") // exige ROLE_USER o ROLE_ADMIN

                .requestMatchers("/api/**").authenticated()
                .requestMatchers("/usuario/historial").authenticated()
                // Rutas de dominio funcional (si son vistas)
                .requestMatchers("/usuarios/historialPaciente").authenticated()
                .requestMatchers("/api/sintomas/**").authenticated()
                .requestMatchers("/sintomas/**").authenticated()
                .requestMatchers("/tratamientos/**").authenticated()
                .requestMatchers("/citas/**").authenticated()
                .requestMatchers("/recordatorios/**").authenticated()
                .requestMatchers("/api/estadisticas/**").authenticated()
                .requestMatchers("/estadisticasUsuario").authenticated()

                //Config Usuarios
                .requestMatchers(HttpMethod.GET,  "/usuarios/configUsuario").authenticated()
                .requestMatchers(HttpMethod.POST, "/usuarios/configUsuario/datos").authenticated()
                .requestMatchers(HttpMethod.POST, "/usuarios/configUsuario/password").authenticated()
                .requestMatchers(HttpMethod.POST, "/usuarios/configUsuario/preferencias").authenticated()

                //Cualquier otra ruta necesita autentificación.
                .anyRequest().authenticated()

        )
        .formLogin(form -> form
                .loginPage("/inicioSesion")
                .loginProcessingUrl("/usuarios/inicioSesion") // POST
                .defaultSuccessUrl("/usuarios/panelUsuario", true)
                .permitAll()
        )
        .logout(logout -> logout
                .logoutUrl("/logout") // POST por defecto
                .logoutSuccessUrl("/inicioSesion")
                .permitAll()
        )

        // MUY IMPORTANTE: que Spring use tu AuthenticationProvider
        .authenticationProvider(authenticationProvider);

        return http.build();
    }

    //Encripta y lee las contraseñas... con BCrypt para usarlo en el login de Spring
    @Bean
    public PasswordEncoder passwordEncoder() { return new CompositePasswordEncoder(); }
    // Expone un objeto de Spring que usa internamente para autenticar usuarios,
    // y lo hace accesible para que el programador lo pueda utilizar también...
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception{
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(UsuariosDetailsServices uds,
                                                         PasswordEncoder encoder) {
        DaoAuthenticationProvider dao = new DaoAuthenticationProvider();
        dao.setUserDetailsService(uds);  // tu servicio que carga el usuario desde BD
        dao.setPasswordEncoder(encoder); // <-- usa tu CompositePasswordEncoder
        return dao;
    }

}