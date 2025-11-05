package com.cooknect.gateway_service.routes;

import com.cooknect.gateway_service.service.JWTService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.function.RequestPredicates;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;
import org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions;
import org.springframework.context.annotation.Bean;

import java.net.URI;

@Configuration
public class Routes {

    @Autowired
    JWTService jwtService;

    @Bean
    public RouterFunction<ServerResponse> userServiceRoute() {
        return GatewayRouterFunctions.route("user-service")
                .route(RequestPredicates.path("/api/v1/users/**"),
                        request -> {
                            String authHeader = request.headers().firstHeader("Authorization");
                            String userEmail;

                            var authentication = SecurityContextHolder.getContext().getAuthentication();
                            if (authentication != null && authentication.isAuthenticated()) {
                                userEmail = (String) authentication.getPrincipal();
                            } else {
                                userEmail = null;
                            }
                            ServerRequest newRequest = ServerRequest.from(request)
                                    .headers(headers -> {
                                        if (authHeader != null) {
                                            headers.set("Authorization", authHeader);
                                            String role = jwtService.extractRole(authHeader.substring(7));
                                            String username = jwtService.extractUsernameField(authHeader.substring(7));
                                            headers.set("X-User-Role", role);
                                            headers.set("X-User-Name", username);
                                        }
                                        if (userEmail != null) {
                                            headers.set("X-User-Email", userEmail);
                                        }
                                    })
                                    .build();
                            return HandlerFunctions.http("http://localhost:8081").handle(newRequest);
                        })
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> userServiceGraphQLRoute() {
        return GatewayRouterFunctions.route("user-service-graphql")
                .route(RequestPredicates.path("/api/users/graphql"),
                        request -> {
                            String authHeader = request.headers().firstHeader("Authorization");
                            String userEmail;

                            var authentication = SecurityContextHolder.getContext().getAuthentication();
                            if (authentication != null && authentication.isAuthenticated()) {
                                userEmail = (String) authentication.getPrincipal();
                            } else {
                                userEmail = null;
                            }

                            ServerRequest newRequest = ServerRequest.from(request)
                                    .headers(headers -> {
                                        if (authHeader != null) {
                                            headers.set("Authorization", authHeader);
                                            String jwtToken = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
                                            String role = jwtService.extractRole(jwtToken);
                                            String username = jwtService.extractUsernameField(jwtToken);
                                            headers.set("X-User-Role", role);
                                            headers.set("X-User-Name", username);
                                        }
                                        if (userEmail != null) {
                                            headers.set("X-User-Email", userEmail);
                                        }
                                    })
                                    .uri(URI.create("http://localhost:8081/graphql"))
                                    .build();

                            return HandlerFunctions.http("http://localhost:8081/graphql").handle(newRequest);
                        })
                .build();
    }
}
