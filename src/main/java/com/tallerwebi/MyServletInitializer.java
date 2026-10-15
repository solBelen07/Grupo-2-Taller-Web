package com.tallerwebi;

import com.tallerwebi.config.DatabaseInitializationConfig;
import com.tallerwebi.config.HibernateConfig;
import com.tallerwebi.config.R2Config;
import com.tallerwebi.config.SpringWebConfig;
import jakarta.servlet.Filter;
import jakarta.servlet.MultipartConfigElement;
import jakarta.servlet.ServletRegistration;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

public class MyServletInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

  // services and data sources
  @Override
  protected Class<?>[] getRootConfigClasses() {
    return new Class[0];
  }

  // controller, view resolver, handler mapping
  @Override
  protected Class<?>[] getServletConfigClasses() {
    return new Class[] {
      SpringWebConfig.class,
      HibernateConfig.class,
      DatabaseInitializationConfig.class,
      R2Config.class,
    };
  }

  @Override
  protected String[] getServletMappings() {
    return new String[] { "/" };
  }

  //metodo que permite caracteres especiales
  @Override
  protected Filter[] getServletFilters() {
    CharacterEncodingFilter characterEncodingFilter = new CharacterEncodingFilter();

    characterEncodingFilter.setEncoding("UTF-8");
    characterEncodingFilter.setForceEncoding(true);

    return new Filter[] { characterEncodingFilter };
  }

  @Override
  protected void customizeRegistration(ServletRegistration.Dynamic registration) {
    long maxFileSize = 50L * 1024 * 1024;
    long maxRequestSize = 55L * 1024 * 1024;

    registration.setMultipartConfig(new MultipartConfigElement("", maxFileSize, maxRequestSize, 0));
  }
}
