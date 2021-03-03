package net.boomerangplatform.service;

import org.springframework.http.MediaType;
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

final class YamlJackson2HttpMessageConverter extends AbstractJackson2HttpMessageConverter {
  YamlJackson2HttpMessageConverter() {
      super(new YAMLMapper().enable(YAMLGenerator.Feature.MINIMIZE_QUOTES), MediaType.parseMediaType("application/x-yaml"));
  }
}