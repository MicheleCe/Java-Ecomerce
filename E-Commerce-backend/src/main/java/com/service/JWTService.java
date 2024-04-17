package com.service;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.model.LocalUser;

import jakarta.annotation.PostConstruct;

@Service
public class JWTService {
	
	private final Set<String> usedTokens = new HashSet<>();

	/** The secret key to encrypt the JWTs with. */
	@Value("${jwt.algorithm.key}")
	private String algorithmKey;
	/** The issuer the JWT is signed with. */
	@Value("${jwt.issuer}")
	private String issuer;
	/** How many seconds from generation should the JWT expire? */
	@Value("${jwt.expiryInSeconds}")
	private int expiryInSeconds;
	/** The algorithm generated post construction. */
	private Algorithm algorithm;
	/** The JWT claim keys. */
	private static final String USERNAME_KEY = "USERNAME";
	private static final String VERIFICATION_EMAIL_KEY = "EMAIL";
	private static final String RESET_PASSWORD_EMAIL_KEY = "EMAIL";

	/**
	 * Post construction method.
	 */
	/**
	 * Post construction method.
	 */
	@PostConstruct
	public void postConstruct() {
		algorithm = Algorithm.HMAC256(algorithmKey);
	}

	/**
	 * Generates a JWT based on the given user.
	 * 
	 * @param user The user to generate for.
	 * @return The JWT.
	 */
	public String generateJWT(LocalUser user) {
		// Create a new JWT token
		return JWT.create()
				// Add a claim to the token payload specifying the username of the user.
				// This claim allows the recipient of the token to identify the user associated
				// with this token.
				// The claim key is defined as USERNAME_KEY and its value is the username of the
				// user passed as a parameter.
				.withClaim(USERNAME_KEY, user.getUsername())

				// Set the expiration time for the token (in milliseconds from the current
				// time).
				// This ensures that the token is valid only for a certain period, after which
				// it expires and becomes invalid.
				.withExpiresAt(new Date(System.currentTimeMillis() + (1000 * expiryInSeconds)))

				// Set the issuer of the token.
				// The issuer is the entity (such as a server or service) that creates and signs
				// the token.
				.withIssuer(issuer)

				// Sign the token with the specified algorithm.
				// This generates the cryptographic signature for the token, ensuring its
				// integrity and authenticity.
				.sign(algorithm);
	}

	/**
	 * Generates a special token for verification of an email.
	 * 
	 * @param user The user to create the token for.
	 * @return The token generated.
	 */
	public String generateVerificationJWT(LocalUser user) {
		return JWT.create().withClaim(VERIFICATION_EMAIL_KEY, user.getEmail())
				.withExpiresAt(new Date(System.currentTimeMillis() + (1000 * expiryInSeconds))).withIssuer(issuer)
				.sign(algorithm);
	}



	/**
	 * Gets the username out of a given JWT.
	 * 
	 * @param token The JWT to decode.
	 * @return The username stored inside.
	 * 
	 *         THIS METHOD IS NOT SECURE ....
	 * 
	 *         This method poses a security risk as it allows potential
	 *         impersonation by simply modifying the USERNAME within a JWT generator
	 *         for an already confirmed account.
	 * 
	 *         public String getUsername(String token) { return
	 *         JWT.decode(token).getClaim(USERNAME_KEY).asString(); }
	 */

	public String getUsername(String token) {
		DecodedJWT jwt = JWT.require(algorithm).withIssuer(issuer).build().verify(token);
		return jwt.getClaim(USERNAME_KEY).asString();
	}

	/**
	 * Generates a JWT for use when resetting a password.
	 * 
	 * @param user The user to generate for.
	 * @return The generated JWT token.
	 */
	public String generatePasswordResetJWT(LocalUser user) {
		return JWT.create().withClaim(RESET_PASSWORD_EMAIL_KEY, user.getEmail())
				.withExpiresAt(new Date(System.currentTimeMillis() + (1000 * 60 * 30))).withIssuer(issuer)
				.sign(algorithm);
	}
	
	/**
	 * Gets the email from a password reset token.
	 * 
	 * @param token The token to use.
	 * @return The email in the token if valid.
	 */
	public String getResetPasswordEmail(String token) {
        if (usedTokens.contains(token)) {
            throw new JWTVerificationException("Token already used");
        }
        usedTokens.add(token);
		DecodedJWT jwt = JWT.require(algorithm).withIssuer(issuer).build().verify(token);
		return jwt.getClaim(RESET_PASSWORD_EMAIL_KEY).asString();
	}
	
    /**
     * Scheduled task to clear the usedTokens set every day.
     */
    @Scheduled(cron = "0 0 0 * * *") // Run at midnight every day
    public void clearUsedTokens() {
        usedTokens.clear();
    }

}
