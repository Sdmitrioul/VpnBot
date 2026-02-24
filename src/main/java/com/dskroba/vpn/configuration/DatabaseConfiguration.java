package com.dskroba.vpn.configuration;

import com.dskroba.vpn.principal.Principal;
import com.dskroba.vpn.principal.PrincipalConverter;
import com.dskroba.vpn.principal.PrincipalHandlersConverter;
import com.dskroba.vpn.property.DatabaseProperties;
import com.dskroba.vpn.statemachine.state.StateContext;
import com.dskroba.vpn.statemachine.state.StateContextConverter;
import com.dskroba.vpn.storage.KeyValueStorage;
import com.dskroba.vpn.storage.LongConverter;
import com.dskroba.vpn.storage.PermanentStorage;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.util.List;

@Configuration
@EnableConfigurationProperties({DatabaseProperties.class})
public class DatabaseConfiguration {
    @Bean
    public KeyValueStorage<Long, Principal> principalStorage(DatabaseProperties properties) {
        return new PermanentStorage<>(new File(properties.path(), properties.principals()), LongConverter.INSTANCE, new PrincipalConverter());
    }

    @Bean
    public KeyValueStorage<Long, StateContext> statesStorage(DatabaseProperties properties) {
        return new PermanentStorage<>(new File(properties.path(), properties.principalStates()), LongConverter.INSTANCE, new StateContextConverter());
    }

    @Bean
    public KeyValueStorage<Long, List<String>> userHandlersStorage(DatabaseProperties properties) {
        return new PermanentStorage<>(new File(properties.path(), properties.principalHandlers()), LongConverter.INSTANCE, new PrincipalHandlersConverter());
    }
}

