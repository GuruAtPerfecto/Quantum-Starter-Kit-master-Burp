@BurpSecurityTest
Feature: Appium Example With Automated Security Scan Feature
  #Sample Test Scenario Description

	@WebGoatSecurityTest_LoginPage
	Scenario: Verify WebGoat login Page with Automated Security Scan using Burp
		Given I Start the BurpScanner
		Then I launch WebGoat Login page
		And I run automated scans, report security issues and download html Burp report
		Then I Close the BurpScanner

	@WebGoatSecurityTest_HomePage
	Scenario: Verify WebGoat Home Page with Automated Security Scan using Burp
		When I start the BurpScanner
		Then I login the WebGoat app with "guestuser" and "guest123" credentials
		And Verify the home page
		Then I run automated scans, report security issues and download html Burp report
		Then I Close the BurpScanner


