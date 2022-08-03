package com.quantum.steps;

import com.qmetry.qaf.automation.step.QAFTestStep;
import com.qmetry.qaf.automation.step.QAFTestStepProvider;
import com.quantum.pages.ExpenseTrackerHomePage;
import com.quantum.pages.ExpenseTrackerLoginPage;
import com.quantum.pages.WebGoatLoginPage;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

@QAFTestStepProvider
public class WebGoatSecutityTestSteps {

//	@Then("I launch WebGoat Login page")
//	public void verifyExpenseTrackerLogin() {
//
//		new WebGoatLoginPage().webGoatLaunchUrl();
//	}


	@QAFTestStep(description="I launch WebGoat Login page")
	public void iLaunchWebGoatLoginPage(){
		new WebGoatLoginPage().webGoatLaunchUrl();
	}

	@QAFTestStep(description="I login the WebGoat app with {0} and {1} credentials")
	public void iLoginTheWebGoatAppWithCredentials(String username,String password){
		new WebGoatLoginPage().webGoatLoginScreen(username, password);
	}

	@QAFTestStep(description="Verify the home page")
	public void verifyTheHomePage() throws Exception {
		new WebGoatLoginPage().verifySuccessfulLogin();
	}

}
