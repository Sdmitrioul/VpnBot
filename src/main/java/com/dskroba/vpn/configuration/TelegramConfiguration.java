package com.dskroba.vpn.configuration;

import com.dskroba.vpn.actor.ActorManager;
import com.dskroba.vpn.principal.PrincipalService;
import com.dskroba.vpn.property.TelegramProperties;
import com.dskroba.vpn.service.TelegramService;
import com.dskroba.vpn.statemachine.EventHandlerRegistry;
import com.dskroba.vpn.telegram.TelegramAuthorizationService;
import com.dskroba.vpn.telegram.TelegramIOModule;
import com.dskroba.vpn.telegram.TelegramStateMachine;
import com.dskroba.vpn.telegram.user.UserHandlersProvider;
import com.pengrad.telegrambot.TelegramBot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration
@EnableConfigurationProperties({TelegramProperties.class})
public class TelegramConfiguration {
    @Bean
    public TelegramBot telegramBot(@Autowired TelegramProperties telegramBotProperties) {
        return new TelegramBot(telegramBotProperties.token());
    }


    @Bean
    public TelegramAuthorizationService authorizationModule(@Autowired PrincipalService principalService,
                                                            @Autowired UserHandlersProvider adminUserProvider) {
        return new TelegramAuthorizationService(principalService, adminUserProvider);
    }

    @Bean
    public Set<String> adminUsers(@Autowired TelegramProperties telegramBotProperties) {
        return Arrays.stream(telegramBotProperties.adminUsers().split(","))
                .collect(Collectors.toSet());
    }

    @Bean
    public TelegramIOModule telegramIOModule(@Autowired TelegramBot bot) {
        return new TelegramIOModule(bot);
    }

    @Bean
    public TelegramStateMachine telegramStateMachine(@Autowired TelegramIOModule ioModule,
                                                     @Autowired EventHandlerRegistry eventHandlerRegistry,
                                                     @Autowired TelegramAuthorizationService authorizationModule,
                                                     @Autowired ActorManager actorManager) {
        return new TelegramStateMachine(ioModule, eventHandlerRegistry, authorizationModule, actorManager);
    }

    @Bean
    public TelegramService telegramContext(@Autowired TelegramBot bot, @Autowired TelegramStateMachine stateMachine) {
        return new TelegramService(bot, stateMachine);
    }
}
