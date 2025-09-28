package be.theking90000.mclib2.config;

import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SchemaValidatorsConfig;
import com.networknt.schema.SpecVersion;

import java.io.IOException;
import java.io.InputStream;

public class JsonSchemaLoader {

    private static final JsonSchemaFactory jsonSchemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
    private static final SchemaValidatorsConfig schemaValidatorsConfig = SchemaValidatorsConfig.builder().build();

    public JsonSchema getSchema(Class<?> cfgClass) {
        try (InputStream in = cfgClass.getClassLoader().getResourceAsStream("schema/" + cfgClass.getCanonicalName() + ".json")) {
            return jsonSchemaFactory.getSchema(in, schemaValidatorsConfig);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
