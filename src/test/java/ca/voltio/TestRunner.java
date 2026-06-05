package ca.voltio;

import static io.cucumber.junit.platform.engine.Constants.*;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

@Suite
@IncludeEngines("cucumber")
@SelectPackages("features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "ca.voltio.stepdefs")
@ConfigurationParameter(key = FILTER_TAGS_PROPERTY_NAME, value = "@Ready and not @NotReady")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME,
                        value = "pretty, json:reports/cucumber-report.json, junit:reports/cucumber-report.junit, html:reports/cucumber-report.html"
)
@ConfigurationParameter(key = PLUGIN_PUBLISH_ENABLED_PROPERTY_NAME, value = "true")
public class TestRunner { }