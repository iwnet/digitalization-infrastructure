/*
 * Copyright 2022 ICCS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gr.ntua.ece.cslab.iwnet.bda.controller;

import gr.ntua.ece.cslab.iwnet.bda.analyticsml.RunnerInstance;
import gr.ntua.ece.cslab.iwnet.bda.common.storage.SystemConnector;
import gr.ntua.ece.cslab.iwnet.bda.common.Configuration;
import gr.ntua.ece.cslab.iwnet.bda.common.storage.SystemConnectorException;
import gr.ntua.ece.cslab.iwnet.bda.controller.cron.CronJobScheduler;
import gr.ntua.ece.cslab.iwnet.bda.controller.connectors.ConsumerApp;

import org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver;
import org.keycloak.adapters.springsecurity.KeycloakConfiguration;
import org.keycloak.adapters.springsecurity.KeycloakSecurityComponents;
import org.keycloak.adapters.springsecurity.authentication.KeycloakAuthenticationProvider;
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;

import java.util.logging.Level;
import java.util.logging.Logger;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
@Import(Entrypoint.SecurityConfig.class)
public class Entrypoint {
    private final static Logger LOGGER = Logger.getLogger(Entrypoint.class.getCanonicalName());
    public static Configuration configuration;
    private static ConfigurableApplicationContext context;

    @org.springframework.context.annotation.Configuration
    public class KeycloakConfig {
        @Bean
        public KeycloakSpringBootConfigResolver KeycloakConfigResolver() {
            return new KeycloakSpringBootConfigResolver();
        }
    }

    @KeycloakConfiguration
    @ConditionalOnProperty("keycloak.enabled")
    @EnableWebSecurity
    @ComponentScan(basePackageClasses = KeycloakSecurityComponents.class,
                   excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX,
                   pattern = "org.keycloak.adapters.springsecurity.management.HttpSessionManager"))
    public class SecurityConfig extends KeycloakWebSecurityConfigurerAdapter {

        @Autowired
        public void configureGlobal(AuthenticationManagerBuilder auth) {

            KeycloakAuthenticationProvider keycloakAuthenticationProvider
                    = keycloakAuthenticationProvider();
            keycloakAuthenticationProvider.setGrantedAuthoritiesMapper(
                    new SimpleAuthorityMapper());
            auth.authenticationProvider(keycloakAuthenticationProvider);
        }

        @Bean
        @Override
        protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
            return new RegisterSessionAuthenticationStrategy(
                    new SessionRegistryImpl());
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            super.configure(http);
            http.csrf().disable();

            //if (configuration.authClientBackend.isSSLEnabled())
            //    http.requiresChannel().anyRequest().requiresSecure().and()
            //        .authorizeRequests()
            //        .anyRequest().authenticated();
            //else
            http.authorizeRequests()
                    .anyRequest().authenticated();

        }
    }


    public static void main(String[] args) throws SystemConnectorException {
        if (args.length < 1) {
            LOGGER.log(Level.WARNING, "Please provide a configuration file as a first argument");
            System.exit(1);
        }
        // parse configuration
        configuration = Configuration.parseConfiguration(args[0]);
        if(configuration==null) {
            System.exit(1);
        }

        SystemConnector.init(args[0]);

        // SIGTERM hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.log(Level.INFO,"Terminating server");
            context.close();
            ConsumerApp.getInstance().close();
            try {
                SystemConnector.getInstance().close();
            } catch (SystemConnectorException e) {
                e.printStackTrace();
            }
        }));

        // start the server and the subscribers
        try {
            LOGGER.log(Level.INFO, "Starting server");
            context = SpringApplication.run(Entrypoint.class, args);
            RunnerInstance.reloadLivySessions();
            ConsumerApp.init();
            CronJobScheduler.init_scheduler();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, e.getMessage());
            e.printStackTrace();
            context.close();
            ConsumerApp.getInstance().close();
            SystemConnector.getInstance().close();
        }
    }
}
