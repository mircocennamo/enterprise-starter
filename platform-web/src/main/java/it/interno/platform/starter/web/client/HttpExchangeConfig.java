package it.interno.platform.starter.web.client;


import it.interno.platform.starter.core.commons.ClientGroup;
import it.interno.platform.starter.security.JwtTokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.support.RestClientHttpServiceGroupConfigurer;
import org.springframework.web.service.registry.ImportHttpServices;

@Configuration
@ImportHttpServices(group = ClientGroup.PINGPONGGROUPNAME, types = {PongClient.class})
public class HttpExchangeConfig {


     @Bean
    public RestClientHttpServiceGroupConfigurer pongServiceConfigurer(JwtTokenProvider jwtTokenProvider) {
        return groups -> groups.filterByName(ClientGroup.PINGPONGGROUPNAME)
                .forEachClient((group, builder) -> {
                    builder.requestInterceptor((request, body, execution) -> {
                        String jwtToken = jwtTokenProvider.getToken(); 
                        if (jwtToken != null && !jwtToken.isEmpty()) {
                            request.getHeaders().setBearerAuth(jwtToken);
                        }
                        return execution.execute(request, body);
                    });
                });
    }


}
