package com.project.selenium;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;

public class App {
    public static void main(String[] args) {
        WebDriverManager.chromedriver().setup();
        WebDriver driver = new ChromeDriver();
        try {
            // login page
            driver.get("https://example.com");
            driver.manage().window().maximize();
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

            // Login
            driver.findElement(By.id("userNameInput")).sendKeys("USERNAME");
            driver.findElement(By.id("passwordInput")).sendKeys("PASSWORD");
            driver.findElement(By.id("submitButton")).click();

            // menu
            try {
                WebElement menuToggle = driver.findElement(By.cssSelector("a.expandCollapseMenu"));
                menuToggle.click();
                Thread.sleep(1000);
                System.out.println("✔ Menu expanded.");
            } catch (Exception e) {
                System.err.println("❌ Menu not expanded: " + e.getMessage());
            }
            // Navigate to Rescheduling screen
            driver.findElement(By.linkText("Maker Desk")).click();
            driver.findElement(By.linkText("Cancellation")).click();
            try {
                WebElement reschedulingLink = driver.findElement(By.id("reschedulingCancellationViewer"));
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", reschedulingLink);
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", reschedulingLink);
                Thread.sleep(1500);
                System.out.println("✔ 'Rescheduling' link clicked via JS.");
            } catch (Exception e) {
                System.err.println("❌ Failed to click 'Rescheduling': " + e.getMessage());
            }
            // Read loan numbers from Excel
            String excelPath = "C:\\example.xlsx";
            List<String> loanNumbers = ExcelReader.getLoanNumbers(excelPath);

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

            for (String loan : loanNumbers) {
                System.out.println("🔍 Searching for loan: " + loan);
                try {
                    WebElement loanField = wait.until(ExpectedConditions.visibilityOfElementLocated(
                            By.id("loanAccountNoForReschedulingCancellation")));
                    loanField.clear();
                    loanField.sendKeys(loan);

                    WebElement searchBtn = driver.findElement(By.id("searchCancellations"));
                    searchBtn.click();
                    System.out.println("🔎 Search button clicked.");
                    Thread.sleep(2000);

                    try {
                        WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(3));

                        // Check if popup exists within short timeout
                        List<WebElement> popups = driver.findElements(By.id("mylendingModal"));

                        if (!popups.isEmpty() && popups.get(0).isDisplayed()) {
                            WebElement popup = popups.get(0);

                            // Check message content
                            WebElement messageElement = popup.findElement(By.cssSelector("#mylendingBody span b"));
                            String popupText = messageElement.getText().trim();

                            if ("No records found for this criteria.".equals(popupText)) {
                                WebElement okButton = popup.findElement(By.id("lendingalertOk"));
                                okButton.click();
                                System.out.println("Popup handled successfully.");
                                continue;
                            }
                        } else {
                            System.out.println("Popup not present.");
                        }
                    } catch (Exception e) {
                        System.out.println("Error while handling popup: " + e.getMessage());
                    }
                    // Click on the reference number link
                    WebElement refLink = wait.until(ExpectedConditions.elementToBeClickable(
                            By.xpath("//td[@columntype='link']/a")));
                    ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", refLink);
                    refLink.click();
                    System.out.println("✔ Clicked on reference number link.");

                    // Click 'Get Revised Schedule'
                    WebElement getRevisedBtn = wait.until(ExpectedConditions.elementToBeClickable(
                            By.id("getRevisedSchedule")));
                    getRevisedBtn.click();
                    System.out.println("✔ Clicked 'Get Revised Schedule'.");

                    // Click OK
                    Thread.sleep(2000);
                    WebElement okBtn = wait.until(ExpectedConditions.elementToBeClickable(
                            By.id("reschedulingCancellationOk")));
                    okBtn.click();
                    System.out.println("✔ Clicked OK after revised schedule.");

                    // Click final "Send for Authorization"
                    Thread.sleep(1000);
                    WebElement sendAuthBtn = driver.findElement(By.id("saveAndSendForAuthorization"));
                    JavascriptExecutor js = (JavascriptExecutor) driver;
                    String display = (String) js.executeScript(
                            "return window.getComputedStyle(arguments[0]).getPropertyValue('display');",
                            sendAuthBtn
                    );

                    if (!"none".equals(display)) {
                        sendAuthBtn.click();
                        System.out.println("Clicked 'Send for Authorization'.");
                    } else {
                        System.out.println("'Send for Authorization' button not visible. Clicking 'Close'.");
                        WebElement closeBtn = driver.findElement(By.id("cancelLoanCloseViewerModal"));
                        closeBtn.click();
                    }
                    System.out.println("✅ Final Send for Authorization done for: " + loan);

                } catch (Exception e) {
                    System.err.println("❌ Error processing loan " + loan + ": " + e.getMessage());
                }

                Thread.sleep(2000); // pause before next loan
            }

        } catch (Exception e) {
            System.err.println("❌ Unexpected error: " + e.getMessage());
        } finally {
            driver.quit();
            System.out.println("🧹 Browser closed.");
        }
    }
}
