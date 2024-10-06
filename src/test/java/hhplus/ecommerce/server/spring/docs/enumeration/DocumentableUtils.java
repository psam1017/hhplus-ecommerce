package hhplus.ecommerce.server.spring.docs.enumeration;

import hhplus.ecommerce.server.domain.common.enumeration.Documentable;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * 자기 환경에 맞게 BASE_PACKAGE_PATH 를 변경하십시오.
 * BASE_PACKAGE_PATH 는 첫 패키지부터 @SpringBootApplication 클래스가 있는 패키지까지의 경로입니다.
 */
public class DocumentableUtils {

    private static final String BASE_PACKAGE_PATH = "hhplus.ecommerce.server";

    public static Map<String, Map<String, String>> findDocumentableInstances() {

        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setScanners(Scanners.SubTypes, Scanners.Resources)
                .addUrls(ClasspathHelper.forJavaClassPath())
                .filterInputsBy(new FilterBuilder().includePackage(BASE_PACKAGE_PATH))
        );
        Set<Class<? extends Documentable>> classes = reflections.getSubTypesOf(Documentable.class);

        Map<String, Map<String, String>> instances = new LinkedHashMap<>();
        for (Class<? extends Documentable> clazz : classes) {
            if (clazz.isEnum()) {
                Documentable[] constants = clazz.getEnumConstants();
                Map<String, String> constantMap = new LinkedHashMap<>();
                for (Documentable constant : constants) {
                    constantMap.put(constant.key(), constant.value());
                }
                instances.put(clazz.getSimpleName(), constantMap);
            }
        }
        return instances;
    }
}
