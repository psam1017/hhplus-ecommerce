package hhplus.ecommerce.server.spring.docs.exception;

import org.springframework.http.MediaType;
import org.springframework.restdocs.operation.Operation;
import org.springframework.restdocs.payload.AbstractFieldsSnippet;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.PayloadSubsectionExtractor;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 이 Snippet 을 사용하기 위해 src/test/resources/org/springframework/restdocs/templates/exception-fields.snippet 를 생성하십시오.
 */
/*
{{title}}
|===
|Key|Value

{{#fields}}
|{{#tableCellContent}}`+{{path}}+`{{/tableCellContent}}
|{{#tableCellContent}}{{description}}{{/tableCellContent}}

{{/fields}}
|===
 */
public class ExceptionFieldSnippet extends AbstractFieldsSnippet {

    public ExceptionFieldSnippet(String type,
                                 List<FieldDescriptor> descriptors,
                                 Map<String, Object> attributes,
                                 boolean ignoreUndocumentedFields,
                                 PayloadSubsectionExtractor<?> subsectionExtractor) {
        super(type, descriptors, attributes, ignoreUndocumentedFields, subsectionExtractor);
    }

    @Override
    protected MediaType getContentType(Operation operation) {
        return operation.getResponse().getHeaders().getContentType();
    }

    @Override
    protected byte[] getContent(Operation operation) throws IOException {
        return operation.getResponse().getContent();
    }
}
