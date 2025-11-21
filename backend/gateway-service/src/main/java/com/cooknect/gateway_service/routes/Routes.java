package com.cooknect.gateway_service.routes;

import com.cooknect.gateway_service.service.JWTService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import java.lang.Long;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.function.RequestPredicates;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;
import org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions;
import org.springframework.context.annotation.Bean;

import static org.springframework.cloud.gateway.server.mvc.filter.FilterFunctions.setPath;

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
                                            Long userId = jwtService.extractUserIdField(authHeader.substring(7));
                                            headers.set("X-User-Role", role);
                                            headers.set("X-User-Id", String.valueOf(userId));
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
    public RouterFunction<ServerResponse> userServiceHealthRoute() {
        return GatewayRouterFunctions.route("user-service-health")
                .route(RequestPredicates.path("/actuator/health/user-service"), HandlerFunctions.http("http://localhost:8081"))
                .filter(setPath("/actuator/health"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> recipeServiceHealthRoute() {
        return GatewayRouterFunctions.route("recipe-service-health")
                .route(RequestPredicates.path("/actuator/health/recipe-service"), HandlerFunctions.http("http://localhost:8082"))
                .filter(setPath("/actuator/health"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> challengeServiceHealthRoute() {
        return GatewayRouterFunctions.route("challenge-service-health")
                .route(RequestPredicates.path("/actuator/health/challenge-service"), HandlerFunctions.http("http://localhost:8083"))
                .filter(setPath("/actuator/health"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> nutritionServiceHealthRoute() {
        return GatewayRouterFunctions.route("nutrition-service-health")
                .route(RequestPredicates.path("/actuator/health/nutrition-service"), HandlerFunctions.http("http://localhost:8085"))
                .filter(setPath("/actuator/health"))
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
                                            Long userId= jwtService.extractUserIdField(jwtToken);
                                            headers.set("X-User-Role", role);
                                            headers.set("X-User-Id", String.valueOf(userId));
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

    @Bean
    public RouterFunction<ServerResponse> recipeServiceRoute() {
        return GatewayRouterFunctions.route("recipe-service")
                .route(RequestPredicates.path("/api/v1/recipes/**"),
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
                                            Long userId = jwtService.extractUserIdField(authHeader.substring(7));
                                            headers.set("X-User-Role", role);
                                            headers.set("X-User-Id", String.valueOf(userId));
                                        }
                                        if (userEmail != null) {
                                            headers.set("X-User-Email", userEmail);
                                        }
                                    })
                                    .build();
                            return HandlerFunctions.http("http://localhost:8082").handle(newRequest);
                        })
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> recipeServiceGraphQLRoute() {
        return GatewayRouterFunctions.route("recipe-service-graphql")
                .route(RequestPredicates.path("/api/recipes/graphql"),
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
                                            Long userId= jwtService.extractUserIdField(jwtToken);
                                            headers.set("X-User-Role", role);
                                            headers.set("X-User-Id", String.valueOf(userId));
                                        }
                                        if (userEmail != null) {
                                            headers.set("X-User-Email", userEmail);
                                        }
                                    })
                                    .uri(URI.create("http://localhost:8082/graphql"))
                                    .build();

                            return HandlerFunctions.http("http://localhost:8082/graphql").handle(newRequest);
                        })
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> challengeServiceRoute() {
        return GatewayRouterFunctions.route("challenge-service")
                .route(RequestPredicates.path("/api/v1/challenges/**"),
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
                                            Long userId = jwtService.extractUserIdField(authHeader.substring(7));
                                            headers.set("X-User-Role", role);
                                            headers.set("X-User-Id", String.valueOf(userId));
                                        }
                                        if (userEmail != null) {
                                            headers.set("X-User-Email", userEmail);
                                        }
                                    })
                                    .build();
                            return HandlerFunctions.http("http://localhost:8083").handle(newRequest);
                        })
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> challengeServiceGraphQLRoute() {
        return GatewayRouterFunctions.route("challenge-service-graphql")
                .route(RequestPredicates.path("/api/challenges/graphql"),
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
                                            Long userId= jwtService.extractUserIdField(jwtToken);
                                            headers.set("X-User-Role", role);
                                            headers.set("X-User-Id", String.valueOf(userId));
                                        }
                                        if (userEmail != null) {
                                            headers.set("X-User-Email", userEmail);
                                        }
                                    })
                                    .uri(URI.create("http://localhost:8083/graphql"))
                                    .build();

                            return HandlerFunctions.http("http://localhost:8083/graphql").handle(newRequest);
                        })
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> notificationServiceRoute() {
        return GatewayRouterFunctions.route("notification-service")
                .route(RequestPredicates.path("/api/v1/notifications/**"),
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
                                            Long userId = jwtService.extractUserIdField(authHeader.substring(7));
                                            headers.set("X-User-Role", role);
                                            headers.set("X-User-Id", String.valueOf(userId));
                                        }
                                        if (userEmail != null) {
                                            headers.set("X-User-Email", userEmail);
                                        }
                                    })
                                    .build();
                            return HandlerFunctions.http("http://localhost:8084").handle(newRequest);
                        })
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> nutritionServiceRoute() {
        return GatewayRouterFunctions.route("nutrition-service")
                .route(RequestPredicates.path("/api/v1/nutrition/**"),
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
                                            Long userId = jwtService.extractUserIdField(authHeader.substring(7));
                                            headers.set("X-User-Role", role);
                                            headers.set("X-User-Id", String.valueOf(userId));
                                        }
                                        if (userEmail != null) {
                                            headers.set("X-User-Email", userEmail);
                                        }
                                    })
                                    .build();
                            return HandlerFunctions.http("http://localhost:8085").handle(newRequest);
                        })
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> nutritionServiceGraphQLRoute() {
        return GatewayRouterFunctions.route("nutrition-service-graphql")
                .route(RequestPredicates.path("/api/nutrition/graphql"),
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
                                            Long userId= jwtService.extractUserIdField(jwtToken);
                                            headers.set("X-User-Role", role);
                                            headers.set("X-User-Id", String.valueOf(userId));
                                        }
                                        if (userEmail != null) {
                                            headers.set("X-User-Email", userEmail);
                                        }
                                    })
                                    .uri(URI.create("http://localhost:8085/graphql"))
                                    .build();

                            return HandlerFunctions.http("http://localhost:8085/graphql").handle(newRequest);
                        })
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> userServiceSwaggerRoute() {
        return GatewayRouterFunctions.route("user-service-swagger")
            .route(RequestPredicates.path("/aggregate/user-service/v3/api-docs"), HandlerFunctions.http("http://localhost:8081"))
            .filter(setPath("/api-docs"))
            .build();
    }

    @Bean
    public RouterFunction<ServerResponse> recipeServiceSwaggerRoute() {
        return GatewayRouterFunctions.route("recipe-service-swagger")
            .route(RequestPredicates.path("/aggregate/recipe-service/v3/api-docs"), HandlerFunctions.http("http://localhost:8082"))
            .filter(setPath("/api-docs"))
            .build();
    }

    @Bean
    public RouterFunction<ServerResponse> challengeServiceSwaggerRoute() {
        return GatewayRouterFunctions.route("challenge-service-swagger")
            .route(RequestPredicates.path("/aggregate/challenge-service/v3/api-docs"), HandlerFunctions.http("http://localhost:8083"))
            .filter(setPath("/api-docs"))
            .build();
    }

    @Bean
    public RouterFunction<ServerResponse> notificationServiceSwaggerRoute() {
        return GatewayRouterFunctions.route("notification-service-swagger")
            .route(RequestPredicates.path("/aggregate/notification-service/v3/api-docs"), HandlerFunctions.http("http://localhost:8084"))
            .filter(setPath("/api-docs"))
            .build();
    }

    @Bean
    public RouterFunction<ServerResponse> nutritionServiceSwaggerRoute() {
        return GatewayRouterFunctions.route("nutrition-service-swagger")
            .route(RequestPredicates.path("/aggregate/nutrition-service/v3/api-docs"), HandlerFunctions.http("http://localhost:8085"))
            .filter(setPath("/api-docs"))
            .build();
    }
}
