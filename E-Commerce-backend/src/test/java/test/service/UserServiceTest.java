package test.service;

import com.ECommerceBackendApplication;
import com.api.model.LoginBody;
import com.api.model.RegistrationBody;
import com.exception.EmailFailureException;
import com.exception.UserAlreadyExistsException;
import com.exception.UserNotVerifiedException;
import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import com.model.VerificationToken;
import com.repository.VerificationTokenDAO;
import com.service.UserService;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Test class to unit test the UserService class.
 */

@SpringBootTest(classes = ECommerceBackendApplication.class)
public class UserServiceTest {

	/** Extension for mocking email sending. */
	@RegisterExtension
	private static GreenMailExtension greenMailExtension = new GreenMailExtension(ServerSetupTest.SMTP)
			.withConfiguration(GreenMailConfiguration.aConfig().withUser("springboot", "secret"))
			.withPerMethodLifecycle(true);

	/** The UserService to test. */
	@Autowired
	private UserService userService;

	/** The Verification Token DAO. */
	@Autowired
	private VerificationTokenDAO verificationTokenDAO;

	/**
	 * Tests the registration process of the user.
	 * 
	 * @throws MessagingException Thrown if the mocked email service fails somehow.
	 */
	@Test
	@Transactional
	public void testRegisterUser() throws MessagingException {
		// Create a RegistrationBody object with test data
		RegistrationBody body = new RegistrationBody();
		body.setUsername("UserA");
		body.setEmail("UserServiceTest$testRegisterUser@junit.com");
		body.setFirstName("FirstName");
		body.setLastName("LastName");
		body.setPassword("MySecretPassword123");

		// Check if registering a user with an existing username throws
		// UserAlreadyExistsException
		Assertions.assertThrows(UserAlreadyExistsException.class, () -> userService.registerUser(body),
				"Username should already be in use.");

		// Change the username to a new one, but keep the email the same
		body.setUsername("UserServiceTest$testRegisterUser");
		body.setEmail("UserA@junit.com");

		// Check if registering a user with an existing email throws
		// UserAlreadyExistsException
		Assertions.assertThrows(UserAlreadyExistsException.class, () -> userService.registerUser(body),
				"Email should already be in use.");

		// Revert the email back to the original one
		body.setEmail("UserServiceTest$testRegisterUser@junit.com");

		// Check if registering a user with unique credentials doesn't throw an
		// exception
		Assertions.assertDoesNotThrow(() -> userService.registerUser(body), "User should register successfully.");

		// Check if the email sent matches the expected recipient
		Assertions.assertEquals(body.getEmail(),
				greenMailExtension.getReceivedMessages()[0].getRecipients(Message.RecipientType.TO)[0].toString());
	}

	/**
	 * Tests the loginUser method.
	 * 
	 * @throws UserNotVerifiedException
	 * @throws EmailFailureException
	 */
	@Test
	@Transactional
	public void testLoginUser() throws UserNotVerifiedException, EmailFailureException {
		LoginBody body = new LoginBody();
		body.setUsername("UserA-NotExists");
		body.setPassword("PasswordA123-BadPassword");
		Assertions.assertNull(userService.loginUser(body), "The user should not exist.");
		body.setUsername("UserA");
		Assertions.assertNull(userService.loginUser(body), "The password should be incorrect.");
		body.setPassword("PasswordA123");
		Assertions.assertNotNull(userService.loginUser(body), "The user should login successfully.");
		body.setUsername("UserB");
		body.setPassword("PasswordB123");
		try {
			userService.loginUser(body);
			Assertions.assertTrue(false, "User should not have email verified.");
		} catch (UserNotVerifiedException ex) {
			Assertions.assertTrue(ex.isNewEmailSent(), "Email verification should be sent.");
			Assertions.assertEquals(1, greenMailExtension.getReceivedMessages().length);
		}
		try {
			userService.loginUser(body);
			Assertions.assertTrue(false, "User should not have email verified.");
		} catch (UserNotVerifiedException ex) {
			Assertions.assertFalse(ex.isNewEmailSent(), "Email verification should not be resent.");
			Assertions.assertEquals(1, greenMailExtension.getReceivedMessages().length);
		}
	}

	/**
	 * Tests the verifyUser method.
	 * 
	 * @throws EmailFailureException
	 */
//  @Test
//  @Transactional
//  public void testVerifyUser() throws EmailFailureException {
//    Assertions.assertFalse(userService.verifyUser("Bad Token"), "Token that is bad or does not exist should return false.");
//    LoginBody body = new LoginBody();
//    body.setUsername("UserB");
//    body.setPassword("PasswordB123");
//    try {
//      userService.loginUser(body);
//      Assertions.assertTrue(false, "User should not have email verified.");
//    } catch (UserNotVerifiedException ex) {
//      List<VerificationToken> tokens = verificationTokenDAO.findByUser_IdOrderByIdDesc(2S);
//      String token = tokens.get(0).getToken();
//      Assertions.assertTrue(userService.verifyUser(token), "Token should be valid.");
//      Assertions.assertNotNull(body, "The user should now be verified.");
//    }
//  }

}
