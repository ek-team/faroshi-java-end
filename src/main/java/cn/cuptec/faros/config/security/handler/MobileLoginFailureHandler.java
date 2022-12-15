package cn.cuptec.faros.config.security.handler;

import cn.cuptec.faros.common.RestResponse;
import cn.hutool.core.util.CharsetUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Slf4j
@Builder
public class MobileLoginFailureHandler implements AuthenticationFailureHandler {

    private static final String BASIC_ = "Basic ";
    private ObjectMapper objectMapper;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws IOException, ServletException {
        try {
            response.setCharacterEncoding(CharsetUtil.UTF_8);
            response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
            response.setStatus(HttpStatus.OK.value());
            PrintWriter printWriter = response.getWriter();
            printWriter.append(objectMapper.writeValueAsString(RestResponse.failed(e.getMessage())));
        } catch (IOException ioException) {
            throw new BadCredentialsException(
                    "Failed to decode basic authentication token");
        }
    }
}
