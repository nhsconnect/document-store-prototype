# Auth (CIS2)

The sequence diagram below illustrate the interactions that occur when a user logs into the app.

The diagram assumes that AWS Amplify has served the React web app (where the sequence begins) and the user has valid
CIS2 login details.

_Note: This diagram does not include interactions with CloudWatch._

```mermaid
sequenceDiagram
    actor GP Practice/PCSE User
    GP Practice/PCSE User ->> React Web App: Clicks start button
    activate React Web App
    React Web App ->> Cognito: Sends auth request
    activate Cognito
    deactivate React Web App
    Cognito ->> CIS2: Forwards auth request
    activate CIS2
    deactivate Cognito
    CIS2 -->> GP Practice/PCSE User: Requests login details
    GP Practice/PCSE User -->> CIS2: Submits login credentials
    CIS2 ->> Cognito: Returns auth code
    activate Cognito
    Cognito ->> CIS2: Requests tokens
    CIS2 ->> Cognito: Returns access & ID tokens
    Cognito ->> CIS2: Requests user info
    CIS2 ->> Cognito: Returns user info
    deactivate CIS2
    Cognito ->> React Web App: Returns access & ID tokens
    activate React Web App
    deactivate Cognito
    React Web App ->> GP Practice/PCSE User: Redirects to /home
    deactivate React Web App
```

The proposal for a new auth flow will look like this:

```mermaid
sequenceDiagram
    actor Browser
    Browser ->> ARF API : /login
    ARF API -->> Browser: Store request state in cookie
    Browser ->> CIS2 : Redirected to CIS2
    CIS2 -->> Browser  : Request Credentials
    Browser ->> CIS2 : Submit Credentials
    CIS2 -->> Browser : Return Auth Code
    Browser ->> ARF API: Redirected to ARF API with auth code and cookie 
    ARF API ->> CIS2 : Request Token
    CIS2 -->> ARF API : Return Access and ID tokens
    ARF API ->> Session Storage : Create session
    ARF API -->> Browser : Redirect with session cookie
    Browser ->> ARF API : Endpoint Request With Cookie
    ARF API ->> Session Storage : Check valid session exists
    ARF API -->> Browser : 200 OK Response
    Browser ->> ARF API : /logout
    ARF API ->> Session Storage : Remove Session
    ARF API -->> Browser : Redirect to Start Page
```
The Back-Channel Logout will allow CIS2 to request the removal of an ongoing session:

```mermaid
sequenceDiagram
    actor Browser
    
    CIS2 ->> ARF API : /BClogout
    ARF API ->> Session Storage : Remove Session
    ARF API -->> CIS2 : 200 OK Response
    Browser ->> ARF API : Endpoint Request With Cookie
    ARF API ->> Session Storage : Check for Valid Session
    ARF API -->> Browser : 401 Auth Error
```