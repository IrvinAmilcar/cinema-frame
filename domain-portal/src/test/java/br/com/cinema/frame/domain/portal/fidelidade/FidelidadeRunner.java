package br.com.cinema.frame.domain.portal.fidelidade;

import org.junit.platform.suite.api.*;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features/fidelidade")
@ConfigurationParameter(key = "cucumber.glue", value = "br.com.cinema.frame.domain.portal.fidelidade")
public class FidelidadeRunner {}

