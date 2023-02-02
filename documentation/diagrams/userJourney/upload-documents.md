# Upload Documents

The user journey diagram below illustrates the journey that the user goes through when uploading documents.

This diagram assumes that has the required permissions to search for a valid NHS number and upload documents.

```mermaid
journey
    title Upload Documents Journey
    section Website Access
        Opens browser: 3: PCSE, GP Practice
        Navigates to website: 4: PCSE, GP Practice
    section Login
        Enters credentials: 3: PCSE, GP Practice
        Redirects to home: 4: PCSE, GP Practice
        Selects to upload: 3: PCSE, GP Practice
    section Patient Search
        Enters NHS number: 3: PCSE, GP Practice
        Confirms patient: 4: PCSE, GP Practice
    section Document Upload
        Selects docs: 3: PCSE, GP Practice
        Waits for upload: 2: PCSE, GP Practice
        Completes upload: 5: PCSE, GP Practice
```
