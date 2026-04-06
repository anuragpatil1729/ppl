package PaymentGateway.springBoot_paymentsApp.controller;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import PaymentGateway.springBoot_paymentsApp.dto.StudentOrder;
import PaymentGateway.springBoot_paymentsApp.service.StudentService;

@Controller
@CrossOrigin(origins = "*") // Useful if you run into CORS issues during local dev
public class StudentController {
    
    @Autowired
    private StudentService service;
    
    // Serve the futuristic index page
    @GetMapping("/")
    public String init() {
        return "index";
    }
    
    /**
     * Creates a Razorpay Order.
     * Note: Ensure your service multiplies 'amount' by 100 
     * to convert Rupees to Paise before calling Razorpay API.
     */
    @PostMapping(value = "/create-order", consumes = "application/json", produces = "application/json")
    @ResponseBody
    public ResponseEntity<StudentOrder> createOrder(@RequestBody StudentOrder studentOrder) throws Exception {
        // Business logic for order creation is delegated to the service
        StudentOrder createdOrder = service.createOrder(studentOrder);
        return new ResponseEntity<>(createdOrder, HttpStatus.CREATED);
    }
    
    /**
     * Handles the callback from Razorpay after payment.
     * Redirects to the success page we just styled.
     */
    @PostMapping("/handle-payment-callback")
    public String handlePaymentCallback(@RequestParam Map<String, String> resPayLoad) {
        
        // Log the response for debugging (highly recommended)
        System.out.println("Razorpay Callback Payload: " + resPayLoad);
        
        // Logic to verify signature and update status (Captured/Failed)
        service.updateOrder(resPayLoad);
        
        // Return the name of your futuristic success HTML file
        return "success"; 
    }
}