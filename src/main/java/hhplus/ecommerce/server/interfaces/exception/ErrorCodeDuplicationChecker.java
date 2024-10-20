package hhplus.ecommerce.server.interfaces.exception;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
public class ErrorCodeDuplicationChecker {

    private static final String BASE_PACKAGE_PATH = "hhplus.ecommerce.server";

    @PostConstruct
    public void check() {

        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setScanners(Scanners.SubTypes, Scanners.Resources)
                .addUrls(ClasspathHelper.forJavaClassPath())
                .filterInputsBy(new FilterBuilder().includePackage(BASE_PACKAGE_PATH))
        );
        Set<Class<? extends ApiException>> classes = reflections.getSubTypesOf(ApiException.class);
        String thisCode = "";
        Map<String, Object> errorCodes = new HashMap<>();
        for (Class<? extends ApiException> clazz : classes) {
            try {
                thisCode = clazz.getSimpleName();
                if (errorCodes.containsKey(thisCode)) {
                    throw new RuntimeException("ErrorCodeDuplicationChecker: ErrorCode is duplicated: " + thisCode);
                }
                errorCodes.put(thisCode, null);
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException("ErrorCodeDuplicationChecker: Error while checking ErrorCode: " + thisCode, e);
            }
        }
    }
}
