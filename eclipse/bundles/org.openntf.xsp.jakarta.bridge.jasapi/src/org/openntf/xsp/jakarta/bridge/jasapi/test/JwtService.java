package org.openntf.xsp.jakarta.bridge.jasapi.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.Date;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.ibm.commons.util.StringUtil;
import com.ibm.designer.domino.napi.NotesDatabase;
import com.ibm.designer.domino.napi.NotesNote;
import com.ibm.designer.domino.napi.NotesSession;
import com.ibm.designer.domino.napi.design.FileAccess;
import com.ibm.domino.bridge.http.jasapi.IJavaSapiEnvironment;
import com.ibm.domino.bridge.http.jasapi.IJavaSapiHttpContextAdapter;
import com.ibm.domino.bridge.http.jasapi.IJavaSapiHttpRequestAdapter;
import com.ibm.domino.bridge.http.jasapi.IJavaSapiHttpResponseAdapter;
import com.ibm.domino.bridge.http.jasapi.JavaSapiService;

public class JwtService extends JavaSapiService {
	public static final String SECRET_NAME = "jwt.txt"; //$NON-NLS-1$
	public static final String ISSUER = "Domino"; //$NON-NLS-1$
	public static final String CLAIM_USER = "UserName"; //$NON-NLS-1$

	public JwtService(IJavaSapiEnvironment env) {
		super(env);
	}

	@Override
	public String getServiceName() {
		return getClass().getSimpleName();
	}

	@Override
	public int authenticate(IJavaSapiHttpContextAdapter context) {
		IJavaSapiHttpRequestAdapter req = context.getRequest();
		
		// In the form of "/foo.nsf/bar"
		String uri = req.getRequestURI();
		String secret = getJwtSecret(uri);
		if(StringUtil.isNotEmpty(secret)) {
			try {
				String auth = req.getHeader("Authorization"); //$NON-NLS-1$
				if(StringUtil.isNotEmpty(auth) && auth.startsWith("Bearer ")) { //$NON-NLS-1$
					String token = auth.substring("Bearer ".length()); //$NON-NLS-1$
					Optional<String> user = decodeAuthenticationToken(token, secret);
					if(user.isPresent()) {
						req.setAuthenticatedUserName(user.get(), "JWT"); //$NON-NLS-1$
						return HTEXTENSION_REQUEST_AUTHENTICATED;
					}
				}
			} catch(Throwable t) {
				t.printStackTrace();
			}
		}
		
		return HTEXTENSION_EVENT_DECLINED;
	}

	@Override
	public void startRequest(IJavaSapiHttpContextAdapter context) {
	}

	@Override
	public int processRequest(IJavaSapiHttpContextAdapter context) {
		return HTEXTENSION_EVENT_DECLINED;
	}

	@Override
	public void endRequest(IJavaSapiHttpContextAdapter context) {
	}

	@Override
	public int rawRequest(IJavaSapiHttpContextAdapter context) {
		IJavaSapiHttpRequestAdapter req = context.getRequest();
		
		String authMe = req.getHeader("X-AuthMe"); //$NON-NLS-1$
		if(StringUtil.isNotEmpty(authMe)) {
			try {
				String uri = req.getRequestURI();
				String secret = getJwtSecret(uri);
				if(StringUtil.isNotEmpty(secret)) {
					
					String token = createAuthenticationToken(authMe, secret, Duration.ofHours(10));
					
					IJavaSapiHttpResponseAdapter resp = context.getResponse();
					resp.setStatus(200);
					resp.setHeader("Content-Type", "text/plain"); //$NON-NLS-1$ //$NON-NLS-2$
					try {
						resp.getOutputStream().print(token);
					} catch (IOException e) {
						e.printStackTrace();
					}
					
					return HTEXTENSION_REQUEST_PROCESSED;
				}
			} catch(Throwable t) {
				t.printStackTrace();
			}
		}
		
		return HTEXTENSION_EVENT_HANDLED;
	}

	@Override
	public int rewriteURL(IJavaSapiHttpContextAdapter context) {
		return HTEXTENSION_EVENT_DECLINED;
	}
	
	@Override
	public int processConsoleCommand(String[] argv, int argc) {
		return HTEXTENSION_EVENT_DECLINED;
	}

	private String getJwtSecret(String uri) {
		int nsfIndex = uri.toLowerCase().indexOf(".nsf"); //$NON-NLS-1$
		if(nsfIndex > -1) {
			String nsfPath = uri.substring(1, nsfIndex+4);
			
			try {
				NotesSession session = new NotesSession();
				try {
					if(session.databaseExists(nsfPath)) {
						// TODO cache lookups and check mod time
						NotesDatabase database = session.getDatabase(nsfPath);
						try {
							database.open();
							NotesNote note = FileAccess.getFileByPath(database, SECRET_NAME);
							if(note != null) {
								try {
									return FileAccess.readFileContentAsString(note);
								} finally {
									note.recycle();
								}
							}
						} finally {
							database.recycle();
						}
					}
				} finally {
					session.recycle();
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public static String createAuthenticationToken(final String userName, final String secret, final Duration duration) {
		if(userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("Cannot create an authentication token for an empty username");
		}
		if(secret == null || secret.isEmpty()) {
			throw new IllegalArgumentException("Cryptographic secret cannot be empty");
		}
		
		try {
			Algorithm algorithm = Algorithm.HMAC256(secret);
			return JWT.create()
					.withIssuer(ISSUER)
					.withClaim(CLAIM_USER, userName)
					.withExpiresAt(Date.from(Instant.now().plus(duration)))
					.sign(algorithm);
		} catch (IllegalArgumentException | UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static Optional<String> decodeAuthenticationToken(final String token, final String secret) {
		if(token == null || token.isEmpty()) {
			return Optional.empty();
		}
		
		try {
			Algorithm algorithm = Algorithm.HMAC256(secret);
			JWTVerifier verifier = JWT.require(algorithm)
			        .withIssuer(ISSUER)
			        .build();
			DecodedJWT jwt = verifier.verify(token);
			Claim claim = jwt.getClaim(CLAIM_USER);
			if(claim != null) {
				return Optional.of(claim.asString());
			} else {
				return Optional.empty();
			}
		} catch (IllegalArgumentException | UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
}
