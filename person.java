package ccim.iar.ui.screen;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

//driver.switchTo().defaultContent(); // you are now outside both frames
//driver.switchTo().frame("cq-cf-frame");

public class Person {

	private static final Logger log = LogManager.getLogger(Person.class);

	public int search(WebDriver driver, SearchCriteria searchcriteria) {

		String action = "Person Search";
		util.logActionStart(driver, action);

		util.waitThnkTime(util.thinkTime);

		moveToSearchFrame(driver);
		WebDriver searchCriteriaFrame = driver.switchTo().frame("SearchFilter");
		// log.debug("Person Search frame " + searchFrame.getPageSource());
		// //SearchFilter

		// set-up search criteria
		String hcnUseField = searchcriteria.getField(SearchCriteria.USE_HCN);
		// hcnUseField = ( hcnUseField == null || hcnUseField.isEmpty() ) ? "0":
		// hcnUseField;
		// boolean useHCN = ((hcnUseField.toUpperCase().equals("YES")) ||
		// hcnUseField.equals("1")) ? true: false;
		boolean useHCN = util.getBoolean(hcnUseField, false);
		String field = null;
		if (useHCN) {
			if ((field = searchcriteria.getField(SearchCriteria.HCN)) != null)
				driver.findElement(By.id("Ontario Health Card Number")).sendKeys(field);
		} else {
			if ((field = searchcriteria.getField(SearchCriteria.LAST_NAME)) != null)
				driver.findElement(By.id("Last Name")).sendKeys(field);
			if ((field = searchcriteria.getField(SearchCriteria.FIRST_NAME)) != null)
				driver.findElement(By.id("First Name")).sendKeys(field);
			if ((field = searchcriteria.getField(SearchCriteria.DOB)) != null)
				driver.findElement(By.id("Date of Birth")).sendKeys(field);
			else
				driver.findElement(By.id("Date of Birth")).sendKeys("01-Oct-2000");
			if ((field = searchcriteria.getField(SearchCriteria.SEX)) != null)
				driver.findElement(By.id("Sex")).sendKeys(field);
			if ((field = searchcriteria.getField(SearchCriteria.STREET)) != null)
				driver.findElement(By.id("Street")).sendKeys(field);
			if ((field = searchcriteria.getField(SearchCriteria.UNIT)) != null)
				driver.findElement(By.id("Unit/Suite/Apt.")).sendKeys(field);
			if ((field = searchcriteria.getField(SearchCriteria.POSTAL_CODE)) != null)
				driver.findElement(By.id("Postal/Zip Code")).sendKeys(field);
			if ((field = searchcriteria.getField(SearchCriteria.PHONE)) != null)
				driver.findElement(By.id("Phone")).sendKeys(field);

		}
		driver.findElement(By.id("default-button-button")).click();

		long startTime = System.currentTimeMillis();

		// if (2==2) return;
		moveToSearchFrame(driver);
		WebDriver searchResultsFrame = driver.switchTo().frame("SearchResults");
		// log.debug("Person Search results before wait " +
		// driver.getPageSource()); //searchFrame

		// Wait for the page to load, timeout after 10 seconds
		(new WebDriverWait(driver, 10)).until(new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver d) {
				return d.findElement(By.id("SearchResults")).findElement(By.id("SearchResults")).isDisplayed();
				// return
				// searchResultsBody.findElement(By.className("data")).isDisplayed()
				// ||
				// searchResultsBody.getText().contains("No result found");
			}
		});

		log.info(action + " duration (sec): " + (System.currentTimeMillis() - startTime) / 1000);

		int rows = 0;
		WebElement searchResultsBody = driver.findElement(By.id("SearchResults")).findElement(By.id("SearchResults"));
		if (!searchResultsBody.getText().contains("No result found")) {
			List<WebElement> searchRows = searchResultsBody.findElements(By.tagName("tr"));
			rows = searchRows.size() - 1;
		}

		log.info(action + " done, found persons " + rows);

		return rows;
	}

	public ArrayList<ArrayList<String>> getDetails(WebDriver driver, int index) {

		String action = "Person getDetails";
		util.logActionStart(driver, action);

		util.waitThnkTime(util.thinkTime);

		moveToSearchFrame(driver);
		driver.switchTo().frame("SearchResults");

		WebElement searchResultsBody = driver.findElement(By.id("search-results-body"));
		log.debug("Person Search Results " + searchResultsBody.getText()); // SearchFilter
		WebElement searchResultsRow = driver.findElement(By.id("row" + index));
		searchResultsRow.click();

		long startTime = System.currentTimeMillis();

		// Wait for the page to load, timeout after 10 seconds
		(new WebDriverWait(driver, 10)).until(new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver d) {
				return !d.getTitle().contains("User");
			}
		});

		log.info(action + " duration (sec): " + (System.currentTimeMillis() - startTime) / 1000);

		ArrayList<ArrayList<String>> assessmentList = util.listAssessments(driver);
		
		log.info(action + " done, now on page " + driver.getTitle());
		
		return assessmentList;

	}

	private WebDriver moveToSearchFrame(WebDriver driver) {
		driver.switchTo().defaultContent(); // you are now outside frames
		driver.switchTo().frame("ConcertoContext");
		driver.switchTo().frame("application-content");
		driver.switchTo().frame(0); // frame("Person Search");
		return driver;
	}
}
