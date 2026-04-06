# Nexus Registration/Login App

## Gmail OTP setup
1. Go to your Google Account -> Security.
2. Enable **2-Step Verification**.
3. Go to **App Passwords**.
4. Generate a password for **Mail**.
5. Use that 16-digit password in `spring.mail.password` in `src/main/resources/application.properties`.
