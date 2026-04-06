package PaymentGateway.springBoot_paymentsApp.user.controller;

import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import PaymentGateway.springBoot_paymentsApp.user.dto.LoginRequest;
import PaymentGateway.springBoot_paymentsApp.user.dto.RegisterRequest;
import PaymentGateway.springBoot_paymentsApp.user.entity.UserEntity;
import PaymentGateway.springBoot_paymentsApp.user.service.UserService;
import PaymentGateway.springBoot_paymentsApp.user.service.UserService.OtpVerificationStatus;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class UserController {

    private static final String PENDING_EMAIL_SESSION_KEY = "pendingVerificationEmail";

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/register")
    public String showRegister(Model model) {
        if (!model.containsAttribute("registerRequest")) {
            model.addAttribute("registerRequest", new RegisterRequest());
        }
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registerRequest") RegisterRequest registerRequest,
            BindingResult bindingResult,
            HttpSession session) {

        if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
            bindingResult.addError(new FieldError("registerRequest", "confirmPassword", "Passwords do not match"));
        }

        String normalizedEmail = registerRequest.getEmail() == null ? "" : registerRequest.getEmail().trim().toLowerCase();
        if (!normalizedEmail.isBlank() && userService.emailExists(normalizedEmail)) {
            bindingResult.addError(new FieldError("registerRequest", "email", "Email already exists"));
        }

        if (bindingResult.hasErrors()) {
            return "register";
        }

        registerRequest.setEmail(normalizedEmail);
        userService.register(registerRequest);
        session.setAttribute(PENDING_EMAIL_SESSION_KEY, normalizedEmail);
        return "redirect:/verify-email";
    }

    @GetMapping("/verify-email")
    public String showVerifyEmail(Model model, HttpSession session) {
        if (session.getAttribute(PENDING_EMAIL_SESSION_KEY) == null) {
            return "redirect:/register";
        }
        return "verify-email";
    }

    @PostMapping("/verify-email")
    public String verifyEmail(@RequestParam("otp") String otp,
            HttpSession session,
            Model model) {
        String pendingEmail = (String) session.getAttribute(PENDING_EMAIL_SESSION_KEY);
        if (pendingEmail == null) {
            return "redirect:/register";
        }

        if (otp == null || !otp.matches("^\\d{6}$")) {
            model.addAttribute("error", "Invalid OTP. Please try again.");
            return "verify-email";
        }

        OtpVerificationStatus status = userService.verifyOtp(pendingEmail, otp);
        if (status == OtpVerificationStatus.VERIFIED) {
            session.removeAttribute(PENDING_EMAIL_SESSION_KEY);
            return "redirect:/login?verified";
        }

        if (status == OtpVerificationStatus.EXPIRED) {
            model.addAttribute("error", "OTP expired. Please register again.");
            return "verify-email";
        }

        model.addAttribute("error", "Invalid OTP. Please try again.");
        return "verify-email";
    }

    @PostMapping("/verify-email/resend")
    public String resendOtp(HttpSession session, Model model) {
        String pendingEmail = (String) session.getAttribute(PENDING_EMAIL_SESSION_KEY);
        if (pendingEmail == null) {
            return "redirect:/register";
        }

        boolean resent = userService.resendOtp(pendingEmail);
        if (resent) {
            model.addAttribute("success", "A new OTP has been sent to your email.");
        } else {
            model.addAttribute("error", "Unable to resend OTP. Please register again.");
        }
        return "verify-email";
    }

    @GetMapping("/login")
    public String showLogin(Model model) {
        if (!model.containsAttribute("loginRequest")) {
            model.addAttribute("loginRequest", new LoginRequest());
        }
        return "login";
    }

    @PostMapping("/login")
    public String login(@Valid @ModelAttribute("loginRequest") LoginRequest loginRequest,
            BindingResult bindingResult,
            HttpSession session) {
        if (bindingResult.hasErrors()) {
            return "login";
        }

        Optional<UserEntity> user = userService.login(loginRequest);
        if (user.isEmpty()) {
            bindingResult.reject("auth.failed", "Invalid email or password");
            return "login";
        }

        if (!user.get().isVerified()) {
            bindingResult.reject("auth.unverified", "Please verify your email first. Check your inbox for the OTP.");
            session.setAttribute(PENDING_EMAIL_SESSION_KEY, user.get().getEmail());
            return "login";
        }

        session.setAttribute("currentUserId", user.get().getId());
        session.setAttribute("currentUserName", user.get().getFullName());
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        Object userName = session.getAttribute("currentUserName");
        Object userId = session.getAttribute("currentUserId");
        if (userName == null || userId == null) {
            return "redirect:/login";
        }

        Optional<UserEntity> user = userService.findById((Long) userId);
        if (user.isEmpty() || !user.get().isVerified()) {
            session.invalidate();
            return "redirect:/login";
        }

        model.addAttribute("currentUserName", userName);
        return "index";
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login?logout";
    }
}
