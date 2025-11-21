package com.cooknect.nutrition_service.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class MdcInterceptor implements HandlerInterceptor {

    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String USER_ID_MDC_KEY = "userId";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String userId = request.getHeader(USER_ID_HEADER);
        if (userId != null && !userId.isEmpty()) {
            MDC.put(USER_ID_MDC_KEY, "userId=" + userId);
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        MDC.remove(USER_ID_MDC_KEY);
    }
}