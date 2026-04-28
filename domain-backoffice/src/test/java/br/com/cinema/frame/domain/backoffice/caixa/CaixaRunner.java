package br.com.cinema.frame.domain.backoffice.caixa;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features/caixa")
@ConfigurationParameter(key = "cucumber.glue", value = "br.com.cinema.frame.domain.backoffice.caixa")
public class CaixaRunner {}