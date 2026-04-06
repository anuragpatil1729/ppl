package PaymentGateway.springBoot_paymentsApp.user.controller;

import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import PaymentGateway.springBoot_paymentsApp.user.dto.LoginRequest;
import PaymentGateway.springBoot_paymentsApp.user.dto.RegisterRequest;
import PaymentGateway.springBoot_paymentsApp.user.entity.UserEntity;
import PaymentGateway.springBoot_paymentsApp.user.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class UserController {

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
            Model model) {

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
        return "redirect:/login?registered";
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

        session.setAttribute("currentUserId", user.get().getId());
        session.setAttribute("currentUserName", user.get().getFullName());
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        Object userName = session.getAttribute("currentUserName");
        if (userName == null) {
            return "redirect:/login";
        }
        model.addAttribute("currentUserName", userName);
        return "index";
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login?logout";
    }
}
