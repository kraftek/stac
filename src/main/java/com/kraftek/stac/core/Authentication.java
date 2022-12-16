package com.kraftek.stac.core;

/**
 * Describes the authentication scheme of a web service.
 *
 * @author Cosmin Cara
 */
public class Authentication {
    private AuthenticationType type;
    private String user;
    private String password;
    private String loginUrl;
    private String authHeader;

    public Authentication() {
        super();
    }

    /**
     * The authentication type.
     * Can be one of: NONE, BASIC, TOKEN
     */
    public AuthenticationType getType() {
        return type;
    }

    public void setType(AuthenticationType type) {
        this.type = type;
    }

    /**
     * The user name
     */
    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    /**
     * The password
     */
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * The login URL, if the authentication type is TOKEN
     */
    public String getLoginUrl() {
        return loginUrl;
    }

    public void setLoginUrl(String loginUrl) {
        this.loginUrl = loginUrl;
    }

    /**
     * The HTTP header name to be used in case of TOKEN authentication
     */
    public String getAuthHeader() {
        return authHeader;
    }

    public void setAuthHeader(String authHeader) {
        this.authHeader = authHeader;
    }
}
