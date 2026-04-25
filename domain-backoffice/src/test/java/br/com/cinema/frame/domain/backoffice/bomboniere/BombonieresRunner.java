package br.com.cinema.frame.domain.backoffice.bomboniere;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features/bomboniere")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "br.com.cinema.frame.domain.backoffice.bomboniere")
public class BombonieresRunner {
}