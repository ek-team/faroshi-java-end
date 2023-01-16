//package cn.cuptec.faros.config.web;
//
//import cn.hutool.core.date.DatePattern;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.springframework.boot.autoconfigure.AutoConfigureBefore;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
//import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
//import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//import java.time.ZoneId;
//import java.util.Locale;
//import java.util.TimeZone;
//3
///**
// * JacksonConfig
// */
//@Configuration 空字段不反回
//@ConditionalOnClass(ObjectMapper.class)
//@AutoConfigureBefore(JacksonAutoConfiguration.class)
//public class JacksonConfig {
//	@Bean
//	public Jackson2ObjectMapperBuilderCustomizer customizer() {
//		return builder -> {
//			builder.locale(Locale.CHINA);
//			builder.timeZone(TimeZone.getTimeZone(ZoneId.systemDefault()));
//			builder.simpleDateFormat(DatePattern.NORM_DATETIME_PATTERN);
//			builder.modules(new JavaTimeModule());
//		};
//	}
//}
