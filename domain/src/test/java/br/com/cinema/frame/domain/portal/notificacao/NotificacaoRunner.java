package br.com.cinema.frame.domain.portal.notificacao;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features/notificacao")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "br.com.cinema.frame.domain.portal.notificacao")
public class NotificacaoRunner {
}
