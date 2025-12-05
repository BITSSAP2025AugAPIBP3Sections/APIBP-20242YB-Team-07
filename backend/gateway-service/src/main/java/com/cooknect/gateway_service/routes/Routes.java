package com.cooknect.gateway_service.routes;

// import com.cooknect.gateway_service.service.JWTService;
import com.cooknect.gateway_service.service.JWTService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.mvc.ProxyExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
// import org.springframework.context.annotation.Configuration;
// import java.lang.Long;
// import org.springframework.security.core.context.SecurityContextHolder;
// import org.springframework.web.servlet.function.RequestPredicates;
// import org.springframework.web.servlet.function.RouterFunction;
// import org.springframework.web.servlet.function.ServerRequest;
// import org.springframework.web.servlet.function.ServerResponse;
// import org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions;
// import org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions;
// import org.springframework.context.annotation.Bean;

// import static org.springframework.cloud.gateway.server.mvc.filter.FilterFunctions.setPath;

// import java.net.URI;

// @Configuration
// public class Routes {

//     @Autowired
//     JWTService jwtService;

//     @Bean
//     public RouterFunction<ServerResponse> userServiceRoute() {
//         return GatewayRouterFunctions.route("user-service")
//                 .route(RequestPredicates.path("/api/v1/users/**"),
//                         request -> {
//                             String authHeader = request.headers().firstHeader("Authorization");
//                             String userEmail;

//                             var authentication = SecurityContextHolder.getContext().getAuthentication();
//                             if (authentication != null && authentication.isAuthenticated()) {
//                                 userEmail = (String) authentication.getPrincipal();
//                             } else {
//                                 userEmail = null;
//                             }
//                             ServerRequest newRequest = ServerRequest.from(request)
//                                     .headers(headers -> {
//                                         if (authHeader != null) {
//                                             headers.set("Authorization", authHeader);
//                                             String role = jwtService.extractRole(authHeader.substring(7));
//                                             Long userId = jwtService.extractUserIdField(authHeader.substring(7));
//                                             headers.set("X-User-Role", role);
//                                             headers.set("X-User-Id", String.valueOf(userId));
//                                         }
//                                         if (userEmail != null) {
//                                             headers.set("X-User-Email", userEmail);
//                                         }
//                                     })
//                                     .build();
//                             return HandlerFunctions.http("http://user-service:8081").handle(newRequest);
//                         })
//                 .build();
//     }

//     @Bean
//     public RouterFunction<ServerResponse> userServiceGraphQLRoute() {
//         return GatewayRouterFunctions.route("user-service-graphql")
//                 .route(RequestPredicates.path("/api/users/graphql"),
//                         request -> {
//                             String authHeader = request.headers().firstHeader("Authorization");
//                             String userEmail;

//                             var authentication = SecurityContextHolder.getContext().getAuthentication();
//                             if (authentication != null && authentication.isAuthenticated()) {
//                                 userEmail = (String) authentication.getPrincipal();
//                             } else {
//                                 userEmail = null;
//                             }

//                             ServerRequest newRequest = ServerRequest.from(request)
//                                     .headers(headers -> {
//                                         if (authHeader != null) {
//                                             headers.set("Authorization", authHeader);
//                                             String jwtToken = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
//                                             String role = jwtService.extractRole(jwtToken);
//                                             Long userId= jwtService.extractUserIdField(jwtToken);
//                                             headers.set("X-User-Role", role);
//                                             headers.set("X-User-Id", String.valueOf(userId));
//                                         }
//                                         if (userEmail != null) {
//                                             headers.set("X-User-Email", userEmail);
//                                         }
//                                     })
//                                     .uri(URI.create("http://localhost:8081/graphql"))
//                                     .build();

//                             return HandlerFunctions.http("http://localhost:8081/graphql").handle(newRequest);
//                         })
//                 .build();
//     }

//     @Bean
//     public RouterFunction<ServerResponse> recipeServiceRoute() {
//         return GatewayRouterFunctions.route("recipe-service")
//                 .route(RequestPredicates.path("/api/v1/recipes/**"),
//                         request -> {
//                             String authHeader = request.headers().firstHeader("Authorization");
//                             String userEmail;

//                             var authentication = SecurityContextHolder.getContext().getAuthentication();
//                             if (authentication != null && authentication.isAuthenticated()) {
//                                 userEmail = (String) authentication.getPrincipal();
//                             } else {
//                                 userEmail = null;
//                             }
//                             ServerRequest newRequest = ServerRequest.from(request)
//                                     .headers(headers -> {
//                                         if (authHeader != null) {
//                                             headers.set("Authorization", authHeader);
//                                             String role = jwtService.extractRole(authHeader.substring(7));
//                                             Long userId = jwtService.extractUserIdField(authHeader.substring(7));
//                                             headers.set("X-User-Role", role);
//                                             headers.set("X-User-Id", String.valueOf(userId));
//                                         }
//                                         if (userEmail != null) {
//                                             headers.set("X-User-Email", userEmail);
//                                         }
//                                     })
//                                     .build();
//                             return HandlerFunctions.http("http://localhost:8082").handle(newRequest);
//                         })
//                 .build();
//     }

//     @Bean
//     public RouterFunction<ServerResponse> recipeServiceGraphQLRoute() {
//         return GatewayRouterFunctions.route("recipe-service-graphql")
//                 .route(RequestPredicates.path("/api/recipes/graphql"),
//                         request -> {
//                             String authHeader = request.headers().firstHeader("Authorization");
//                             String userEmail;

//                             var authentication = SecurityContextHolder.getContext().getAuthentication();
//                             if (authentication != null && authentication.isAuthenticated()) {
//                                 userEmail = (String) authentication.getPrincipal();
//                             } else {
//                                 userEmail = null;
//                             }

//                             ServerRequest newRequest = ServerRequest.from(request)
//                                     .headers(headers -> {
//                                         if (authHeader != null) {
//                                             headers.set("Authorization", authHeader);
//                                             String jwtToken = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
//                                             String role = jwtService.extractRole(jwtToken);
//                                             Long userId= jwtService.extractUserIdField(jwtToken);
//                                             headers.set("X-User-Role", role);
//                                             headers.set("X-User-Id", String.valueOf(userId));
//                                         }
//                                         if (userEmail != null) {
//                                             headers.set("X-User-Email", userEmail);
//                                         }
//                                     })
//                                     .uri(URI.create("http://localhost:8082/graphql"))
//                                     .build();

//                             return HandlerFunctions.http("http://localhost:8082/graphql").handle(newRequest);
//                         })
//                 .build();
//     }

//     @Bean
//     public RouterFunction<ServerResponse> challengeServiceRoute() {
//         return GatewayRouterFunctions.route("challenge-service")
//                 .route(RequestPredicates.path("/api/v1/challenges/**"),
//                         request -> {
//                             String authHeader = request.headers().firstHeader("Authorization");
//                             String userEmail;

//                             var authentication = SecurityContextHolder.getContext().getAuthentication();
//                             if (authentication != null && authentication.isAuthenticated()) {
//                                 userEmail = (String) authentication.getPrincipal();
//                             } else {
//                                 userEmail = null;
//                             }
//                             ServerRequest newRequest = ServerRequest.from(request)
//                                     .headers(headers -> {
//                                         if (authHeader != null) {
//                                             headers.set("Authorization", authHeader);
//                                             String role = jwtService.extractRole(authHeader.substring(7));
//                                             Long userId = jwtService.extractUserIdField(authHeader.substring(7));
//                                             headers.set("X-User-Role", role);
//                                             headers.set("X-User-Id", String.valueOf(userId));
//                                         }
//                                         if (userEmail != null) {
//                                             headers.set("X-User-Email", userEmail);
//                                         }
//                                     })
//                                     .build();
//                             return HandlerFunctions.http("http://localhost:8083").handle(newRequest);
//                         })
//                 .build();
//     }

//     @Bean
//     public RouterFunction<ServerResponse> challengeServiceGraphQLRoute() {
//         return GatewayRouterFunctions.route("challenge-service-graphql")
//                 .route(RequestPredicates.path("/api/challenges/graphql"),
//                         request -> {
//                             String authHeader = request.headers().firstHeader("Authorization");
//                             String userEmail;

//                             var authentication = SecurityContextHolder.getContext().getAuthentication();
//                             if (authentication != null && authentication.isAuthenticated()) {
//                                 userEmail = (String) authentication.getPrincipal();
//                             } else {
//                                 userEmail = null;
//                             }

//                             ServerRequest newRequest = ServerRequest.from(request)
//                                     .headers(headers -> {
//                                         if (authHeader != null) {
//                                             headers.set("Authorization", authHeader);
//                                             String jwtToken = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
//                                             String role = jwtService.extractRole(jwtToken);
//                                             Long userId= jwtService.extractUserIdField(jwtToken);
//                                             headers.set("X-User-Role", role);
//                                             headers.set("X-User-Id", String.valueOf(userId));
//                                         }
//                                         if (userEmail != null) {
//                                             headers.set("X-User-Email", userEmail);
//                                         }
//                                     })
//                                     .uri(URI.create("http://localhost:8083/graphql"))
//                                     .build();

//                             return HandlerFunctions.http("http://localhost:8083/graphql").handle(newRequest);
//                         })
//                 .build();
//     }

//     @Bean
//     public RouterFunction<ServerResponse> nutritionServiceRoute() {
//         return GatewayRouterFunctions.route("nutrition-service")
//                 .route(RequestPredicates.path("/api/v1/nutrition/**"),
//                         request -> {
//                             String authHeader = request.headers().firstHeader("Authorization");
//                             String userEmail;

//                             var authentication = SecurityContextHolder.getContext().getAuthentication();
//                             if (authentication != null && authentication.isAuthenticated()) {
//                                 userEmail = (String) authentication.getPrincipal();
//                             } else {
//                                 userEmail = null;
//                             }
//                             ServerRequest newRequest = ServerRequest.from(request)
//                                     .headers(headers -> {
//                                         if (authHeader != null) {
//                                             headers.set("Authorization", authHeader);
//                                             String role = jwtService.extractRole(authHeader.substring(7));
//                                             Long userId = jwtService.extractUserIdField(authHeader.substring(7));
//                                             headers.set("X-User-Role", role);
//                                             headers.set("X-User-Id", String.valueOf(userId));
//                                         }
//                                         if (userEmail != null) {
//                                             headers.set("X-User-Email", userEmail);
//                                         }
//                                     })
//                                     .build();
//                             return HandlerFunctions.http("http://localhost:8085").handle(newRequest);
//                         })
//                 .build();
//     }

//     @Bean
//     public RouterFunction<ServerResponse> nutritionServiceGraphQLRoute() {
//         return GatewayRouterFunctions.route("nutrition-service-graphql")
//                 .route(RequestPredicates.path("/api/nutrition/graphql"),
//                         request -> {
//                             String authHeader = request.headers().firstHeader("Authorization");
//                             String userEmail;

//                             var authentication = SecurityContextHolder.getContext().getAuthentication();
//                             if (authentication != null && authentication.isAuthenticated()) {
//                                 userEmail = (String) authentication.getPrincipal();
//                             } else {
//                                 userEmail = null;
//                             }

//                             ServerRequest newRequest = ServerRequest.from(request)
//                                     .headers(headers -> {
//                                         if (authHeader != null) {
//                                             headers.set("Authorization", authHeader);
//                                             String jwtToken = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
//                                             String role = jwtService.extractRole(jwtToken);
//                                             Long userId= jwtService.extractUserIdField(jwtToken);
//                                             headers.set("X-User-Role", role);
//                                             headers.set("X-User-Id", String.valueOf(userId));
//                                         }
//                                         if (userEmail != null) {
//                                             headers.set("X-User-Email", userEmail);
//                                         }
//                                     })
//                                     .uri(URI.create("http://localhost:8085/graphql"))
//                                     .build();

//                             return HandlerFunctions.http("http://localhost:8085/graphql").handle(newRequest);
//                         })
//                 .build();
//     }

//     @Bean
//     public RouterFunction<ServerResponse> userServiceSwaggerRoute() {
//         return GatewayRouterFunctions.route("user-service-swagger")
//             .route(RequestPredicates.path("/aggregate/user-service/v3/api-docs"), HandlerFunctions.http("http://localhost:8081"))
//             .filter(setPath("/api-docs"))
//             .build();
//     }

//     @Bean
//     public RouterFunction<ServerResponse> recipeServiceSwaggerRoute() {
//         return GatewayRouterFunctions.route("recipe-service-swagger")
//             .route(RequestPredicates.path("/aggregate/recipe-service/v3/api-docs"), HandlerFunctions.http("http://localhost:8082"))
//             .filter(setPath("/api-docs"))
//             .build();
//     }

//     @Bean
//     public RouterFunction<ServerResponse> challengeServiceSwaggerRoute() {
//         return GatewayRouterFunctions.route("challenge-service-swagger")
//             .route(RequestPredicates.path("/aggregate/challenge-service/v3/api-docs"), HandlerFunctions.http("http://localhost:8083"))
//             .filter(setPath("/api-docs"))
//             .build();
//     }

//     @Bean
//     public RouterFunction<ServerResponse> nutritionServiceSwaggerRoute() {
//         return GatewayRouterFunctions.route("nutrition-service-swagger")
//             .route(RequestPredicates.path("/aggregate/nutrition-service/v3/api-docs"), HandlerFunctions.http("http://localhost:8085"))
//             .filter(setPath("/api-docs"))
//             .build();
//     }
// }


// @RestController
// public class Routes {

//     private final JWTService jwtService;
//     private final RestTemplate restTemplate;

//     @Autowired
//     public Routes(JWTService jwtService) {
//         this.jwtService = jwtService;
//         this.restTemplate = new RestTemplate();
//     }

//     @GetMapping("/api/v1/users/**")
//     public ResponseEntity<String> proxyUsersGET(HttpServletRequest request) {
//         return forwardRequest(request, null, HttpMethod.GET);
//     }

//     @PostMapping("/api/v1/users/**")
//     public ResponseEntity<String> proxyUsersPOST(HttpServletRequest request, @RequestBody(required = false) String body) {
//         return forwardRequest(request, body, HttpMethod.POST);
//     }

//     @PutMapping("/api/v1/users/**")
//     public ResponseEntity<String> proxyUsersPUT(HttpServletRequest request, @RequestBody(required = false) String body) {
//         return forwardRequest(request, body, HttpMethod.PUT);
//     }

//     @DeleteMapping("/api/v1/users/**")
//     public ResponseEntity<String> proxyUsersDELETE(HttpServletRequest request) {
//         return forwardRequest(request, null, HttpMethod.DELETE);
//     }

//     private ResponseEntity<String> forwardRequest(HttpServletRequest request, String body, HttpMethod method) {
//         String originalPath = request.getRequestURI();
//         String queryString = request.getQueryString();
//         String url = "http://localhost:8081" + originalPath;
//         if (queryString != null) {
//             url += "?" + queryString;
//         }

//         org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
//         headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

//         String authHeader = request.getHeader("Authorization");
//         if (authHeader != null && authHeader.startsWith("Bearer ")) {
//             String token = authHeader.substring(7);
//             headers.set("Authorization", authHeader);
//             headers.set("X-User-Role", jwtService.extractRole(token));
//             headers.set("X-User-Id", String.valueOf(jwtService.extractUserIdField(token)));
//             headers.set("X-User-Email", jwtService.extractUserName(token));
//         }

//         org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(body, headers);

//         try {
//             return restTemplate.exchange(url, method, entity, String.class);
//         } catch (org.springframework.web.client.HttpClientErrorException | org.springframework.web.client.HttpServerErrorException e) {
//             return ResponseEntity
//                     .status(e.getStatusCode())
//                     .body(e.getResponseBodyAsString());
//         } catch (Exception e) {
//             return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
//                     .body("{\"error\": \"Error forwarding request: " + e.getMessage() + "\"}");
//         }
//     }
// }

import org.springframework.beans.factory.annotation.Value;

@RestController
public class Routes {

    private final JWTService jwtService;
    private final RestTemplate restTemplate;
    
    // Service URLs - use environment variables for flexibility
    private final String USER_SERVICE_URL;
    private final String RECIPE_SERVICE_URL;
    private final String CHALLENGE_SERVICE_URL;
    private final String NUTRITION_SERVICE_URL;

    @Autowired
    public Routes(JWTService jwtService, 
                  @Value("${services.user.url:http://localhost:8081}") String userServiceUrl,
                  @Value("${services.recipe.url:http://localhost:8082}") String recipeServiceUrl,
                  @Value("${services.challenge.url:http://localhost:8083}") String challengeServiceUrl,
                  @Value("${services.nutrition.url:http://localhost:8085}") String nutritionServiceUrl) {
        this.jwtService = jwtService;
        this.restTemplate = new RestTemplate();
        this.USER_SERVICE_URL = userServiceUrl;
        this.RECIPE_SERVICE_URL = recipeServiceUrl;
        this.CHALLENGE_SERVICE_URL = challengeServiceUrl;
        this.NUTRITION_SERVICE_URL = nutritionServiceUrl;
    }

    @GetMapping("/api/v1/users/**")
    public ResponseEntity<String> proxyUsersGET(HttpServletRequest request) {
        return forwardRequest(request, null, HttpMethod.GET, USER_SERVICE_URL);
    }

    @PostMapping("/api/v1/users/**")
    public ResponseEntity<String> proxyUsersPOST(HttpServletRequest request, @RequestBody(required = false) String body) {
        return forwardRequest(request, body, HttpMethod.POST, USER_SERVICE_URL);
    }

    @PutMapping("/api/v1/users/**")
    public ResponseEntity<String> proxyUsersPUT(HttpServletRequest request, @RequestBody(required = false) String body) {
        return forwardRequest(request, body, HttpMethod.PUT, USER_SERVICE_URL);
    }

    @DeleteMapping("/api/v1/users/**")
    public ResponseEntity<String> proxyUsersDELETE(HttpServletRequest request) {
        return forwardRequest(request, null, HttpMethod.DELETE, USER_SERVICE_URL);
    }

    // Recipe service routes
    @GetMapping("/api/v1/recipes/**")
    public ResponseEntity<String> proxyRecipesGET(HttpServletRequest request) {
        return forwardRequest(request, null, HttpMethod.GET, RECIPE_SERVICE_URL);
    }

    @PostMapping("/api/v1/recipes/**")
    public ResponseEntity<String> proxyRecipesPOST(HttpServletRequest request, @RequestBody(required = false) String body) {
        return forwardRequest(request, body, HttpMethod.POST, RECIPE_SERVICE_URL);
    }

    @PutMapping("/api/v1/recipes/**")
    public ResponseEntity<String> proxyRecipesPUT(HttpServletRequest request, @RequestBody(required = false) String body) {
        return forwardRequest(request, body, HttpMethod.PUT, RECIPE_SERVICE_URL);
    }

    @DeleteMapping("/api/v1/recipes/**")
    public ResponseEntity<String> proxyRecipesDELETE(HttpServletRequest request) {
        return forwardRequest(request, null, HttpMethod.DELETE, RECIPE_SERVICE_URL);
    }

    // Challenge service routes
    @GetMapping("/api/v1/challenges/**")
    public ResponseEntity<String> proxyChallengesGET(HttpServletRequest request) {
        return forwardRequest(request, null, HttpMethod.GET, CHALLENGE_SERVICE_URL);
    }

    @PostMapping("/api/v1/challenges/**")
    public ResponseEntity<String> proxyChallengesPOST(HttpServletRequest request, @RequestBody(required = false) String body) {
        return forwardRequest(request, body, HttpMethod.POST, CHALLENGE_SERVICE_URL);
    }

    @PutMapping("/api/v1/challenges/**")
    public ResponseEntity<String> proxyChallengesPUT(HttpServletRequest request, @RequestBody(required = false) String body) {
        return forwardRequest(request, body, HttpMethod.PUT, CHALLENGE_SERVICE_URL);
    }

    @DeleteMapping("/api/v1/challenges/**")
    public ResponseEntity<String> proxyChallengesDELETE(HttpServletRequest request) {
        return forwardRequest(request, null, HttpMethod.DELETE, CHALLENGE_SERVICE_URL);
    }

    // Nutrition service routes
    @GetMapping("/api/v1/nutrition/**")
    public ResponseEntity<String> proxyNutritionGET(HttpServletRequest request) {
        return forwardRequest(request, null, HttpMethod.GET, NUTRITION_SERVICE_URL);
    }

    @PostMapping("/api/v1/nutrition/**")
    public ResponseEntity<String> proxyNutritionPOST(HttpServletRequest request, @RequestBody(required = false) String body) {
        return forwardRequest(request, body, HttpMethod.POST, NUTRITION_SERVICE_URL);
    }

    private ResponseEntity<String> forwardRequest(HttpServletRequest request, String body, HttpMethod method, String serviceUrl) {
        String originalPath = request.getRequestURI();
        String queryString = request.getQueryString();
        String url = serviceUrl + originalPath;
        if (queryString != null) {
            url += "?" + queryString;
        }

        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            headers.set("Authorization", authHeader);
            headers.set("X-User-Role", jwtService.extractRole(token));
            headers.set("X-User-Id", String.valueOf(jwtService.extractUserIdField(token)));
            headers.set("X-User-Email", jwtService.extractUserName(token));
        }

        org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(body, headers);

        try {
            return restTemplate.exchange(url, method, entity, String.class);
        } catch (org.springframework.web.client.HttpClientErrorException | org.springframework.web.client.HttpServerErrorException e) {
            return ResponseEntity
                    .status(e.getStatusCode())
                    .body(e.getResponseBodyAsString());
        } catch (Exception e) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Error forwarding request: " + e.getMessage() + "\"}");
        }
    }
}
