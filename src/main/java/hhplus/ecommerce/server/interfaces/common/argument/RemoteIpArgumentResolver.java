package hhplus.ecommerce.server.interfaces.common.argument;

import hhplus.ecommerce.server.interfaces.common.clientinfo.ClientInfoHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Slf4j
@RequiredArgsConstructor
public class RemoteIpArgumentResolver implements HandlerMethodArgumentResolver {

    private final ClientInfoHolder clientInfoHolder;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {

        boolean hasRemoteIpAnnotation = parameter.hasParameterAnnotation(RemoteIp.class);
        boolean hasStringType = String.class.isAssignableFrom(parameter.getParameterType());
        return hasRemoteIpAnnotation && hasStringType;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        return clientInfoHolder.getRemoteIp();
    }
}
