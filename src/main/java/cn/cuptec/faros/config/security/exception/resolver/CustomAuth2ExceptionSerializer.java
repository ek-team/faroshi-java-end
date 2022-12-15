package cn.cuptec.faros.config.security.exception.resolver;

import cn.cuptec.faros.common.constrants.CommonConstants;
import cn.cuptec.faros.config.security.exception.CustomAuth2Exception;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import lombok.SneakyThrows;

/**
 * OAuth2 异常格式化
 */
public class CustomAuth2ExceptionSerializer extends StdSerializer<CustomAuth2Exception> {
	public CustomAuth2ExceptionSerializer() {
		super(CustomAuth2Exception.class);
	}

	@Override
	@SneakyThrows
	public void serialize(CustomAuth2Exception value, JsonGenerator gen, SerializerProvider provider) {
		gen.writeStartObject();
		gen.writeObjectField("code", value.getErrorCode() != null ? value.getErrorCode() : CommonConstants.FAIL);
		gen.writeStringField("msg", value.getMessage());
		gen.writeStringField("data", value.getErrorCode());
		gen.writeEndObject();
	}
}
