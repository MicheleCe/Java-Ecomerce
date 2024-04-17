package com.api.security;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authorization.AuthorizationEventPublisher;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.SpringAuthorizationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.messaging.access.intercept.AuthorizationChannelInterceptor;
import org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import com.model.LocalUser;
import com.service.UserService;

import java.util.Map;
import java.util.UUID;

/**
 * Class to configur spring websockets.
 */
@Configuration
@EnableWebSocket
@EnableWebSocketMessageBroker
public class WebsocketConfiguration implements WebSocketMessageBrokerConfigurer {

	/** The Application Context. */
	private ApplicationContext context;
	/** The JWT Request Filter. */
	private JWTRequestFilter jwtRequestFilter;
	/** The User Service. */
	private UserService userService;
	/** Matcher instance. */
	private static final AntPathMatcher MATCHER = new AntPathMatcher();

	/**
	 * Default constructor for spring injection.
	 * 
	 * @param context
	 * @param jwtRequestFilter
	 * @param userService
	 */
	public WebsocketConfiguration(ApplicationContext context, JWTRequestFilter jwtRequestFilter,
			UserService userService) {
		this.context = context;
		this.jwtRequestFilter = jwtRequestFilter;
		this.userService = userService;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		// Registering a WebSocket endpoint "/websocket" and allowing all origins
		registry.addEndpoint("/websocket").setAllowedOriginPatterns("**").withSockJS();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		// Configuring message broker to use "/topic" for broadcasting messages
		registry.enableSimpleBroker("/topic");
		// Configuring application destination prefix "/app" for message handling
		registry.setApplicationDestinationPrefixes("/app");
	}

	/**
	 * Creates an AuthorizationManager for managing authentication required for
	 * specific channels.
	 * 
	 * @return The AuthorizationManager object.
	 */
	// Method to create an AuthorizationManager for managing authentication required
	// for specific channels
	private AuthorizationManager<Message<?>> makeMessageAuthorizationManager() {
	    // Builder for MessageMatcherDelegatingAuthorizationManager
	    MessageMatcherDelegatingAuthorizationManager.Builder messages = new MessageMatcherDelegatingAuthorizationManager.Builder();
	    // Configuring authorization for specific destinations
	    messages
	        // Authentication required for user-related topics
	        .simpDestMatchers("/topic/user/**").authenticated()
	        // Authentication required for product-related topics
	        // Permit all other messages
	        .anyMessage().permitAll();
	    // Building and returning AuthorizationManager
	    return messages.build();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void configureClientInboundChannel(ChannelRegistration registration) {
		// Creating an AuthorizationManager for message authentication
		AuthorizationManager<Message<?>> authorizationManager = makeMessageAuthorizationManager();
		// Creating AuthorizationChannelInterceptor with provided AuthorizationManager
		AuthorizationChannelInterceptor authInterceptor = new AuthorizationChannelInterceptor(authorizationManager);
		// Creating SpringAuthorizationEventPublisher
		AuthorizationEventPublisher publisher = new SpringAuthorizationEventPublisher(context);
		// Setting AuthorizationEventPublisher to authInterceptor
		authInterceptor.setAuthorizationEventPublisher(publisher);
		// Adding interceptors for client inbound channel
		registration.interceptors(jwtRequestFilter, authInterceptor,
				new RejectClientMessagesOnChannelsChannelInterceptor(),
				new DestinationLevelAuthorizationChannelInterceptor());
	}

	/**
	 * Interceptor for rejecting client messages on specific channels.
	 */
	private class RejectClientMessagesOnChannelsChannelInterceptor implements ChannelInterceptor {

		/** Paths that do not allow client messages. */
		// Define an array of paths where client messages are not allowed
		private String[] paths = new String[] { "/topic/user/*/address" };

		/**
		 * {@inheritDoc}
		 */
		// Override the preSend method from the ChannelInterceptor interface
		@Override
		public Message<?> preSend(Message<?> message, MessageChannel channel) {
			// Check if the message type is MESSAGE
			if (message.getHeaders().get("simpMessageType").equals(SimpMessageType.MESSAGE)) {
				// Extract the destination from the message headers
				String destination = (String) message.getHeaders().get("simpDestination");
				// Iterate over the paths array
				for (String path : paths) {
					// Check if the destination matches any path
					if (MATCHER.match(path, destination))
						// If there's a match, set the message to null, rejecting it
						message = null;
				}
			}
			// Return the modified or unmodified message
			return message;
		}
	}

	/**
	 * Interceptor to apply authorization and permissions onto specific channels and
	 * path variables.
	 */
	private class DestinationLevelAuthorizationChannelInterceptor implements ChannelInterceptor {

		/**
		 * This method is called before a message is sent through a channel. It checks
		 * if the message is a subscription message and if it matches a specific
		 * pattern. *
		 * @param message The WebSocket message being sent.
		 * @param channel The channel through which the message is being sent.
		 * @return The modified message or null if the message is blocked.
		 */
		@Override
		public Message<?> preSend(Message<?> message, MessageChannel channel) {
			if (message.getHeaders().get("simpMessageType").equals(SimpMessageType.SUBSCRIBE)) {
				// Extract the destination from the message headers
				String destination = (String) message.getHeaders().get("simpDestination");
				// Define a pattern for user-specific topics
				String userTopicMatcher = "/topic/user/{userId}/**";
				// Check if the destination matches the user-specific topic pattern
				if (MATCHER.match(userTopicMatcher, destination)) {
					// Extract parameters from the destination using the defined pattern
					Map<String, String> params = MATCHER.extractUriTemplateVariables(userTopicMatcher, destination);
					try {
						// Extract the user ID from the parameters
						UUID userId = UUID.fromString(params.get("userId"));
						// Get the authentication object from the security context
						Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
						// Check if the user is authenticated
						if (authentication != null) {
							// Get the authenticated user
							LocalUser user = (LocalUser) authentication.getPrincipal();
							// Check if the authenticated user has permission to access the specified user's
							// data
							if (!userService.userHasPermissionToUser(user, userId)) {
								// If the user does not have permission, set the message to null to block it
								message = null;
							}
						} else {
							// If there is no authentication, set the message to null to block it
							message = null;
						}
					} catch (NumberFormatException ex) {
						// If there's an error parsing the user ID, set the message to null to block it
						message = null;
					}
				}
			}
			// Return the message, which may have been modified or set to null
			return message;
		}
	}
}