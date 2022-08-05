/**
 * 
 */
package com.quantum.steps;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.OutputType;

import com.perfecto.reportium.client.ReportiumClient;
import com.qmetry.qaf.automation.core.ConfigurationManager;
import com.qmetry.qaf.automation.step.QAFTestStepProvider;
import com.qmetry.qaf.automation.ui.WebDriverTestBase;
import com.qmetry.qaf.automation.ui.webdriver.QAFExtendedWebElement;
import com.qmetry.qaf.automation.util.StringUtil;
import com.quantum.burpIntegration.BurpClient;
import com.quantum.burpIntegration.domain.ReportType;
import com.quantum.burpIntegration.domain.ScanIssue;
import com.quantum.burpIntegration.domain.ScanIssueList;
import com.quantum.utils.AppiumUtils;
import com.quantum.utils.ConfigurationUtils;
import com.quantum.utils.ConsoleUtils;
import com.quantum.utils.DeviceUtils;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;


@QAFTestStepProvider
public class CommonStepsDefs {


	@Then("I switch to frame \"(.*?)\"")
	public static void switchToFrame(String nameOrIndex) {
		if (StringUtil.isNumeric(nameOrIndex)) {
			int index = Integer.parseInt(nameOrIndex);
			new WebDriverTestBase().getDriver().switchTo().frame(index);
		} else {
			new WebDriverTestBase().getDriver().switchTo().frame(nameOrIndex);
		}
	}

	@Then("I switch to \"(.*?)\" frame by element")
	public static void switchToFrameByElement(String loc) {
		new WebDriverTestBase().getDriver().switchTo().frame(new QAFExtendedWebElement(loc));
	}

	@When("I am using an AppiumDriver")
	public void testForAppiumDriver() {
		if (ConfigurationUtils.getBaseBundle().getPropertyValue("driver.name").contains("Remote"))
			ConsoleUtils.logWarningBlocks("Driver is an instance of QAFExtendedWebDriver");
		else if (AppiumUtils.getAppiumDriver() instanceof IOSDriver)
			ConsoleUtils.logWarningBlocks("Driver is an instance of IOSDriver");
		else if (AppiumUtils.getAppiumDriver() instanceof AndroidDriver)
			ConsoleUtils.logWarningBlocks("Driver is an instance of AndroidDriver");
	}
	
	@Then("I Start the BurpScanner")
	public static void startBurp() throws IOException {
		if(ConfigurationManager.getBundle().getString("includeSecurityTests", "false").equalsIgnoreCase("true")){
			String burpServerAddress = (String) ConfigurationManager.getBundle().getProperty("burpServerAddress");
			String burpServerPort = (String) ConfigurationManager.getBundle().getProperty("burpServerPort");
			String burpConfigFile = (String) ConfigurationManager.getBundle().getProperty("burpConfigFile");


			BurpClient burpClient;

			burpServerAddress = burpServerAddress.replaceFirst("^(http[s]?://)","");


			burpClient = new BurpClient("http://" + burpServerAddress + ":" + burpServerPort);
			burpClient.terminateBurp();
			burpClient.startBurp(burpServerAddress, burpServerPort, burpConfigFile);

			ConfigurationManager.getBundle().setProperty("burpClient", burpClient);
		}


	}	
	

	@Then("I Close the BurpScanner")
	public static void closeBurp() throws IOException {
		if(ConfigurationManager.getBundle().getString("includeSecurityTests", "false").equalsIgnoreCase("true")) {
			getBurpClient().terminateBurp();
		}

	}	
	
	@Then("I run automated scans, report security issues and download html Burp report")
	public static void scanAndDownloadReport() throws Exception {
		if(ConfigurationManager.getBundle().getString("includeSecurityTests", "false").equalsIgnoreCase("true")) {

			//String AUTUrl = (String) ConfigurationManager.getBundle().getProperty("AUTUrl");
			String ResultString = null;
			
			String URLString = DeviceUtils.getQAFDriver().getCurrentUrl();

			Pattern regex = Pattern.compile("(http):\\/\\/(.+?)\\/");
			Matcher regexMatcher = regex.matcher(URLString);
			if (regexMatcher.find()) {
				ResultString = regexMatcher.group();
				System.out.println("url1 = " + ResultString);
			}

			getBurpClient().includeInScope(StringUtil.chop(ResultString));
			getBurpClient().spider(StringUtil.chop(ResultString));
			getBurpClient().scan(StringUtil.chop(ResultString));

			System.out.println("Status = " + getBurpClient().getScannerStatus());

			//https://github.com/vmware/burp-rest-api/issues/95
			//getPercentageComplete() is deprecated by burp. So, sometimes it always gives 0 Status. Temperorarily waiting 1 miutes to rpocess
			int count = 0;
			while(getBurpClient().getScannerStatus() != 100){
				System.out.println("Scan Status = " +getBurpClient().getScannerStatus());
				Thread.sleep(3000);
				count += 1;
				//some times tools struck with 0 status. Closing after 20 seconds
				if (count >= 30)
					break;
			}

			SimpleDateFormat formatter = new SimpleDateFormat("ddMMyyyyHHmmss");
			Date date = new Date();
			System.out.println(formatter.format(date));

			String currentTestName = (String) ConfigurationManager.getBundle().getProperty("current.testcase.name");

			File createDir = new File("burp/" + currentTestName  + "_" +  date);
			createDir.mkdir();

			getBurpClient().getReportData(ReportType.HTML, StringUtil.chop(ResultString), "burp/" + currentTestName  + "_" +  date + "/BurpReport_" + date + ".html" );
			ScanIssueList IssueList = getBurpClient().getScanIssues(StringUtil.chop(ResultString));

			reportSecurityIssues(IssueList);

			//Ensure nothing is running on given port
			getBurpClient().terminateBurp();
		}

	}	
	
	private static void reportSecurityIssues(ScanIssueList IssueList) throws Exception {
		Boolean violationsFound = false;
        List<ScanIssue> list = IssueList.getScanIssues();
        ReportiumClient reportiumClient = (ReportiumClient) ConfigurationManager.getBundle().getObject("perfecto.report.client");

        String perfectoReportMessage = null;

        System.out.println("Total Security Issues found = " + list.size());
        if(list.size() > 0) {
			DeviceUtils.getQAFDriver().getScreenshotAs(OutputType.BASE64);
			violationsFound = true;
		}

        for(int i=0; i<list.size(); i++){

            perfectoReportMessage = String.format("Security Issue: "+ (i+1) +System.lineSeparator() + "URL: %s%n; Issue Name: %s%n; Issue Type: %s%n; Severity:\t%s%n; Confidence:\t\t%s%n%n; IssueBackGround:\t\t%s%n%n;",
                    list.get(i).getUrl(),list.get(i).getIssueName(), list.get(i).getIssueType(), list.get(i).getSeverity(), list.get(i).getConfidence(), list.get(i).getIssueBackground());
            
            reportiumClient.reportiumAssert(perfectoReportMessage, false);
            System.out.println(perfectoReportMessage);
        }
		if(violationsFound)
			throw new Exception("Security Vulnerability found on the current page. Please check the report");
    }

	private static BurpClient getBurpClient(){

		return (BurpClient) ConfigurationManager.getBundle().getProperty("burpClient");
	}
}