package com.quantum.pages;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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

public class ExpenseTrackerLoginPage extends WebDriverBaseTestPage<WebDriverTestPage> {

	@Override
	protected void openPage(PageLocator locator, Object... args) {
		// TODO Auto-generated method stub
		
	}

	@FindBy(locator = "login.email.textfield")
	private QAFWebElement logintextfiled;
	
	@FindBy(locator = "login.emailNative.textfield")
	private QAFWebElement emailNativeTextfield;
	
	@FindBy(locator = "login.headerTextNative.label")
	private QAFWebElement headerTextNative;
	
	@FindBy(locator = "login.passwordNative.textfield")
	private QAFWebElement passwordlNativeTextfield;
	
	@FindBy(locator = "login.loginNative.btn")
	private QAFWebElement loginlNativeButton;
	
	@FindBy(locator = "login.email.textFieldValue")
	private QAFWebElement loginlEmailTextFieldValue;

	@FindBy(locator = "exptrackerweb.txt.username")
	private QAFWebElement webUsernameField;

	@FindBy(locator = "exptrackerweb.txt.password")
	private QAFWebElement webPasswordField;

	@FindBy(locator = "exptrackerweb.btn.loginbtn")
	private QAFWebElement webLoginBtn;

	@FindBy(locator = "exptrackerweb.btn.hamburgbtn")
	private QAFWebElement webHamurgBtn;

	@FindBy(locator = "exptrackerweb.btn.logourbtn")
	private QAFWebElement webLogourBtn;


	public void verifyExpenseTrackerLoginScreen() {
		Map<String, Object> params = new HashMap<>();
		params.put("content", "Email");
		params.put("timeout", "15");
		driver.executeScript("mobile:text:find", params);
	}
	
	public void verifyExpenseTrackerNativeLoginScreen() {
		if(DriverUtils.getDriver().getCapabilities().getCapability("platformName").toString().equalsIgnoreCase("ios")) {
			QAFExtendedWebElement ele = new QAFExtendedWebElement(By.xpath("//*[@label='OK']"));
			try {
				ele.click();
			} catch (Exception e) {
			}
		}
		ReportUtils.logAssert("Verify Login screen title", headerTextNative.isDisplayed());
		ReportUtils.logAssert("Verify Login screen Email", emailNativeTextfield.isDisplayed());
	}
	
	public void loginNative(String email, String password) {
		emailNativeTextfield.sendKeys(email);
		ReportUtils.logAssert("Email was entered as expected", loginlEmailTextFieldValue.getText().equalsIgnoreCase(email));
		passwordlNativeTextfield.sendKeys(password);
		DriverUtils.getAppiumDriver().hideKeyboard();
		loginlNativeButton.click();
	}

	public void launchExpTrackerWeb(){
		DeviceUtils.getQAFDriver().get("http://expensetracker.perfectomobile.com/#/app-login");
	}

	public void loginExpTrackerWeb(String username, String password){
		webUsernameField.sendKeys(username);
		webPasswordField.sendKeys(password);
		webLoginBtn.click();
	}

	public void verifySuccessfulLogin() throws Exception {

		Map<String, Object> params = new HashMap<>();
		params.put("content", "Logout");
		params.put("threshold", 80);
		params.put("imageBounds.needleBound", 30);
		params.put("timeout", "25");
		String res = (String)driver.executeScript("mobile:checkpoint:text", params);

		if (!res.equalsIgnoreCase("true")) {
			//successful checkpoint code
			throw new Exception("WebGoat Login Unsuccessful. Please check");
		}
	}

	public void loginOutExpTrackerWeb(){
		try {
			webHamurgBtn.click();
		} catch (Exception e){ }
		webLogourBtn.click();
	}
}
