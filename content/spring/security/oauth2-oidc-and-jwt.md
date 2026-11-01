---
title: "Authentication and Authorization with OAuth 2.0, OpenID Connect and JWT"
linkTitle: "OAuth2, OIDC & JWT"
description: "Modern identity for Spring applications: authentication vs authorization, OAuth 2.0 roles and grant types (authorization code with PKCE, client credentials), OAuth 2.1 changes, OpenID Connect and ID tokens, JWT structure and validation, Spring Boot resource servers and OAuth2 clients, mapping scopes and roles, the BFF pattern for SPAs, token storage, and Spring Authorization Server."
weight: 2
difficulty: intermediate
status: complete
javaVersion: 21
tags: [spring-security, oauth2, oidc, jwt]
keywords: [oauth2 explained, openid connect vs oauth2, authorization code flow pkce, client credentials grant, jwt validation spring boot, oauth2 resource server spring, oauth2 client spring boot, jwt claims scopes roles, bff pattern spa security, spring authorization server, oauth 2.1, token introspection, keycloak spring boot]
lastReviewed: 2026-11-01
prerequisites:
  - /spring/security/security-filter-chain
related:
  - /spring/security/method-level-security
  - /microservices/fundamentals/api-gateway
---

## Simple definition

- **Authentication** proves **who** someone is. **Authorization** decides **what they may do**.
- **OAuth 2.0** is a framework for **delegated authorization**: an app obtains an **access token** to call an API on a user's behalf, or on its own behalf.
- **OpenID Connect (OIDC)** adds an **authentication** layer on top: an **ID token** that says who logged in.
- **JWT (JSON Web Token)** is a common, self-contained, signed token format.

## The problem

Every service managing its own passwords leads to weak security, no single sign-on and no MFA. Sharing user passwords with third-party apps is unacceptable. OAuth 2.0 and OIDC centralise login in an **authorization server** (identity provider) and give apps and APIs **scoped, short-lived tokens** instead.

## Roles

| Role | Example |
|---|---|
| **Resource owner** | The user |
| **Client** | Your web app, SPA backend or mobile app, or a service calling another service |
| **Authorization server (IdP)** | Keycloak, Microsoft Entra ID, Okta, Auth0, Spring Authorization Server |
| **Resource server** | Your Spring API that accepts access tokens |

## Grant types (flows)

| Flow | For | Status |
|---|---|---|
| **Authorization code + PKCE** | Users logging in to web, mobile and SPA (via a BFF) | ✅ **The standard** |
| **Client credentials** | Service-to-service, with no user | ✅ |
| Refresh token | Getting new access tokens without re-login | ✅ (rotate them) |
| Device authorization | TVs, CLIs | ✅ |
| Token exchange (RFC 8693) | Delegation across services (on-behalf-of) | ✅ Advanced |
| Implicit, resource owner password | — | ❌ **Removed in OAuth 2.1** |

{{< interactive-diagram file="oauth2-code-flow.html" title="OAuth 2.0 authorization code flow with PKCE" source="diagrams/archify/oauth2-code-flow.sequence.json" tool="Archify" height="720" >}}

**PKCE** (Proof Key for Code Exchange): the client sends a hash of a random secret (`code_challenge`) and later proves possession with the secret (`code_verifier`). An intercepted authorization code is then useless. **OAuth 2.1** (consolidating best practice) requires PKCE for all clients.

## Tokens

| Token | Audience | Contains | Lifetime |
|---|---|---|---|
| **Access token** | The API (resource server) | Scopes, subject, audience, expiry. Often a JWT, or opaque. | Short (5–60 min) |
| **ID token** (OIDC) | The **client** | Identity claims (`sub`, `name`, `email`, `auth_time`) | Short; **never send it to APIs** |
| **Refresh token** | The authorization server | An opaque handle | Longer; rotate and revoke |

### JWT structure and validation

`header.payload.signature`, each part base64url-encoded:

```json
{ "alg": "RS256", "kid": "2026-10" }
{ "iss": "https://id.shop.example", "sub": "u-42", "aud": "orders-api",
  "scope": "orders:read orders:write", "roles": ["CUSTOMER"], "exp": 1793530000, "iat": 1793526400 }
```

A resource server must check:

- the **signature**, using the issuer's public keys from the **JWKS** endpoint (cached; rotate by `kid`)
- `iss`, `aud`, `exp` and `nbf`
- then the scopes or roles for authorization.

Prefer asymmetric algorithms (RS256, ES256, EdDSA), and reject `alg: none` and unexpected algorithms.

## Spring Boot as a resource server

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: https://id.shop.example/realms/shop      # discovers JWKS, validates iss
          audiences: orders-api                                 # validates aud
```

```java
@Bean
JwtAuthenticationConverter jwtAuthConverter() {
    var authorities = new JwtGrantedAuthoritiesConverter();      // "scope" claim → SCOPE_orders:read
    var roles = new JwtGrantedAuthoritiesConverter();
    roles.setAuthoritiesClaimName("roles");                      // custom claim → ROLE_CUSTOMER
    roles.setAuthorityPrefix("ROLE_");
    var converter = new JwtAuthenticationConverter();
    converter.setJwtGrantedAuthoritiesConverter(jwt -> {
        var all = new ArrayList<>(authorities.convert(jwt));
        all.addAll(roles.convert(jwt));
        return all;
    });
    return converter;
}
```

Then authorise with `hasAuthority("SCOPE_orders:write")` and `hasRole("ADMIN")`, in the filter chain or with [method security](/spring/security/method-level-security). **Opaque tokens** use **introspection** (`opaquetoken.introspection-uri`) instead: a network call per request, but instantly revocable.

## Spring Boot as an OAuth2 client

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          shop:                      # user login (OIDC)
            provider: keycloak
            client-id: shop-web
            client-secret: ${SHOP_WEB_SECRET}
            scope: openid, profile, orders:read
          inventory:                 # service-to-service
            provider: keycloak
            client-id: orders-service
            client-secret: ${ORDERS_SECRET}
            authorization-grant-type: client_credentials
        provider:
          keycloak:
            issuer-uri: https://id.shop.example/realms/shop
```

- `oauth2Login()` gives OIDC login for server-rendered apps and BFFs.
- **`RestClient` / `WebClient` with `OAuth2ClientHttpRequestInterceptor`** (Spring Security 6.4+) automatically obtains and attaches client-credentials tokens for service calls.

## SPAs: the Backend-for-Frontend (BFF) pattern

Storing tokens in browser JavaScript (localStorage) exposes them to XSS. The current recommendation (IETF *OAuth 2.0 for Browser-Based Apps*): a **BFF**, a server-side component (Spring Boot or Spring Cloud Gateway with `TokenRelay`) that does the OAuth flow, keeps the tokens **server-side**, and gives the SPA an **HttpOnly, Secure, SameSite session cookie**, with CSRF protection.

## Running your own authorization server

**Spring Authorization Server** (now part of Spring Security 7) implements OAuth 2.1 and OIDC, which is useful when you must host identity yourself. Most teams use Keycloak or a managed IdP instead of building one.

## Common mistakes

> [!MISTAKE] Sending ID tokens to APIs
> ID tokens are for the client. APIs need access tokens with the correct audience.

> [!MISTAKE] Not validating the audience
> A token issued for another API would be accepted. Configure `audiences`.

> [!MISTAKE] Long-lived access tokens in the browser
> Stolen tokens stay valid for hours. Use short lifetimes, refresh rotation and a BFF.

> [!MISTAKE] Putting authorisation data the API cannot trust in the token
> Only the IdP-issued, signed claims count. Never trust client-supplied headers for identity.

## Quick check

**OAuth 2.0 vs OpenID Connect?**
OAuth 2.0 is delegated authorization (access tokens for APIs). OIDC adds authentication: an ID token and a userinfo endpoint saying who the user is.

**Why is PKCE needed?**
It binds the authorization code to the client that started the flow, so a stolen code cannot be exchanged for tokens.

**Which grant type do services use to call each other?**
Client credentials, or token exchange when acting on behalf of a user.

**What must a resource server validate in a JWT?**
The signature (via JWKS), issuer, audience, expiry and not-before, then scopes or roles for authorization.

**Why use a BFF for SPAs?**
It keeps tokens server-side and gives the browser only an HttpOnly session cookie, reducing token theft through XSS.

**Which grants does OAuth 2.1 remove?**
The implicit grant and the resource owner password credentials grant.

## Summary

- Centralise login in an IdP; APIs accept short-lived, audience-bound access tokens.
- Authorization code + PKCE for users; client credentials for services; BFF for SPAs.
- Spring Boot resource servers validate JWTs from `issuer-uri`; map scopes and roles to authorities.
- Follow OAuth 2.1 best practice: PKCE everywhere, no implicit or password grants.

## References

- Spring Security reference, [OAuth2 Resource Server JWT](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html) and [OAuth2 Client](https://docs.spring.io/spring-security/reference/servlet/oauth2/client/index.html).
- IETF, [OAuth 2.1 draft](https://datatracker.ietf.org/doc/draft-ietf-oauth-v2-1/), [RFC 7636 (PKCE)](https://www.rfc-editor.org/rfc/rfc7636), [RFC 9700: OAuth 2.0 Security Best Current Practice](https://www.rfc-editor.org/rfc/rfc9700).
- IETF, [OAuth 2.0 for Browser-Based Applications](https://datatracker.ietf.org/doc/draft-ietf-oauth-browser-based-apps/).
- [OpenID Connect Core 1.0](https://openid.net/specs/openid-connect-core-1_0.html).
