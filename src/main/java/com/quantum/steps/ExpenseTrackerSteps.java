package com.quantum.steps;

import com.qmetry.qaf.automation.step.QAFTestStep;
import com.qmetry.qaf.automation.step.QAFTestStepProvider;
import com.quantum.pages.ExpenseTrackerHomePage;
import com.quantum.pages.ExpenseTrackerLoginPage;

import com.quantum.pages.WebGoatLoginPage;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

@QAFTestStepProvider
public class ExpenseTrackerSteps {

	@Then("I should see expense tracker login screen")
	public void verifyExpenseTrackerLogin() {
		new ExpenseTrackerLoginPage().verifyExpenseTrackerLoginScreen();
	}
	
	@Then("I should see expense tracker Native login screen")
	public void verifyExpenseTrackerNativeLogin() {
		new ExpenseTrackerLoginPage().verifyExpenseTrackerNativeLoginScreen();
	}
	
	@When("I enter \"(.*?)\" and \"(.*?)\" in native login screen")
	public void iEnterLoginDetilsInNativeLoginScreen(String email, String password) {
		new ExpenseTrackerLoginPage().loginNative(email, password);
	}
	
	
	@Then("I should see expense tracker home screen")
	public void iShouldSeeExpenseTrackerHomeScreen() {
		new ExpenseTrackerHomePage().verifyHomeScreen();
	}
	
	@When("I enter expense details and save")
	public void iEnterExpenseDetailsAndSave() {
		new ExpenseTrackerHomePage().enterExpenseDetails();
	}
	
	@Then("I should see error popup")
	public void iShouldSeeErrorPopup() {
		new ExpenseTrackerHomePage().verifyPopupText(); 
	}


	@QAFTestStep(description="I launch Exp Tracker Login page")
	public void iLaunchExpTrackerLoginPage(){
		new ExpenseTrackerLoginPage().launchExpTrackerWeb();
	}

	@QAFTestStep(description="I login the Exp tracker web app with {0} and {1} credentials")
	public void iLoginTheExpTrackerAppWithCredentials(String username,String password){
		new ExpenseTrackerLoginPage().loginExpTrackerWeb(username, password);
	}

	@QAFTestStep(description="Verify Expense Tracker home page")
	public void verityHomePage() throws Exception {
		new ExpenseTrackerLoginPage().verifySuccessfulLogin();
	}

	@QAFTestStep(description="I logout from the Webapplication")
	public void logOutfromtheWebApplication() throws Exception {
		new ExpenseTrackerLoginPage().loginOutExpTrackerWeb();
	}

}
