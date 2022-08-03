package com.quantum.pages;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.qmetry.qaf.automation.core.ConfigurationManager;
import org.openqa.selenium.By;

import com.qmetry.qaf.automation.ui.WebDriverBaseTestPage;
import com.qmetry.qaf.automation.ui.annotations.FindBy;
import com.qmetry.qaf.automation.ui.api.PageLocator;
import com.qmetry.qaf.automation.ui.api.WebDriverTestPage;
import com.qmetry.qaf.automation.ui.webdriver.QAFExtendedWebElement;
import com.qmetry.qaf.automation.ui.webdriver.QAFWebElement;
import com.quantum.utils.DeviceUtils;
import com.quantum.utils.DriverUtils;
import com.quantum.utils.ReportUtils;
import org.openqa.selenium.OutputType;

public class WebGoatLoginPage extends WebDriverBaseTestPage<WebDriverTestPage> {

	@Override
	protected void openPage(PageLocator locator, Object... args) {
		// TODO Auto-generated method stub
		
	}


	@FindBy(locator = "login.text.username")
	private QAFWebElement loginUserNamefield;
	
	@FindBy(locator = "login.text.password")
	private QAFWebElement loginPasswordfield;
	
	@FindBy(locator = "login.btn.loginbtn")
	private QAFWebElement loginSignInButton;
	
	
	public void verifySuccessfulLogin() throws Exception {
		
		Map<String, Object> params = new HashMap<>();
		params.put("content", "Challenges");
		params.put("threshold", 80);
		//params.put("imageBounds.needleBound", 30);
		params.put("timeout", "25");
		String res = (String)driver.executeScript("mobile:checkpoint:text", params);

		if (!res.equalsIgnoreCase("true")) {
		    //successful checkpoint code
			throw new Exception("WebGoat Login Unsuccessful. Please check");
		}
		DeviceUtils.getQAFDriver().getScreenshotAs(OutputType.BASE64);
	}

	public void webGoatLoginScreen(String username, String password) {
		loginUserNamefield.sendKeys(username);
		loginPasswordfield.sendKeys(password);
		loginSignInButton.click();
	}

	public void webGoatLaunchUrl() {
		String webGoatServerAddress = (String) ConfigurationManager.getBundle().getProperty("webGoatServerAddress");
		String webGoatServerPort = (String) ConfigurationManager.getBundle().getProperty("webGoatServerPort");

		webGoatServerAddress = webGoatServerAddress.replaceFirst("^(http[s]?://)","");

		DeviceUtils.getQAFDriver().get("http://" + webGoatServerAddress + ":" + webGoatServerPort + "/WebGoat/login");
	}
}
