package hhplus.ecommerce.server.spring.docs.exception;

import hhplus.ecommerce.server.interfaces.common.api.ApiException;
import hhplus.ecommerce.server.interfaces.common.api.ApiResponse;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * 자기 환경에 맞게 BASE_PACKAGE_PATH 를 변경하십시오.
 * BASE_PACKAGE_PATH 는 첫 패키지부터 @SpringBootApplication 클래스가 있는 패키지까지의 경로입니다.
 */
@SuppressWarnings("unchecked")
public class ApiExceptionUtils {

    private static final String BASE_PACKAGE_PATH = "hhplus.ecommerce.server";

    public static Map<String, String> findApiExceptionInstances() {

        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setScanners(Scanners.SubTypes, Scanners.Resources)
                .addUrls(ClasspathHelper.forJavaClassPath())
                .filterInputsBy(new FilterBuilder().includePackage(BASE_PACKAGE_PATH))
        );
        Set<Class<? extends ApiException>> classes = reflections.getSubTypesOf(ApiException.class);

        Map<String, String> instances = new LinkedHashMap<>();
        for (Class<? extends ApiException> clazz : classes) {
            String debug;

            try {
                ApiException apiExceptionInstance = clazz.getDeclaredConstructor().newInstance();
                Field responseField = ApiException.class.getDeclaredField("response");
                responseField.setAccessible(true);
                ApiResponse<Object> response = (ApiResponse<Object>) responseField.get(apiExceptionInstance);
                debug = response.getDebug();
            } catch (Exception e) {
                debug = "";
            }
            
            // Store the value in the map with the class name as the key
            instances.put(clazz.getSimpleName(), debug);
        }

        // key 순서로 정렬하고 응답
        return instances.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(LinkedHashMap::new, (m, e) -> m.put(e.getKey(), e.getValue()), Map::putAll);
    }
}
