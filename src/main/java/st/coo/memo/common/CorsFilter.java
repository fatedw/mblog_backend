package st.coo.memo.common;

import jakarta.annotation.Resource;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import st.coo.memo.service.SysConfigService;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
@WebFilter(urlPatterns = {"/*"}, filterName = "corsFilter")
public class CorsFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.info("启动跨域过滤器");
    }

    @Resource
    private SysConfigService sysConfigService;

    @Value("${safe.domain:}")
    private String safeDomain;

    @Override
    public void doFilter(ServletRequest request, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
        HttpServletResponse response = (HttpServletResponse) resp;

        String domains = sysConfigService.getString(SysConfigConstant.CORS_DOMAIN_LIST);
        if (StringUtils.isEmpty(domains)) {
            domains = safeDomain;
        } else {
            domains = domains + "," + safeDomain;
        }
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        String origin = httpServletRequest.getHeader("Origin");
        List<String> domainList = Arrays.stream(domains.split(",")).map(String::trim).collect(Collectors.toList());
        if (StringUtils.hasText(domains) &&
                StringUtils.hasText(origin) && domainList.contains(origin)) {
            // 手动设置响应头解决跨域访问
            response.setHeader("Access-Control-Allow-Origin", origin);
            response.setHeader("Access-Control-Allow-Methods", "POST, PUT, GET, OPTIONS, DELETE");
            // 设置过期时间
            response.setHeader("Access-Control-Max-Age", "86400");
            response.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept, token");
            // 支持 HTTP 1.1
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            // 支持 HTTP 1.0. response.setHeader("Expires", "0");
            response.setHeader("Pragma", "no-cache");
        }

        // 编码
        response.setCharacterEncoding("UTF-8");
        chain.doFilter(request, resp);
    }

    @Override
    public void destroy() {
        log.info("销毁跨域过滤器");
    }

}
