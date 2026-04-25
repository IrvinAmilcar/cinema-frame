package br.com.cinema.frame.domain.backoffice.classificacao;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features/classificacao")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "br.com.cinema.frame.domain.backoffice.classificacao")
public class ClassificacaoRunner {
}
