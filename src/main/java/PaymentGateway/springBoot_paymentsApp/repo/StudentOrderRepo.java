package PaymentGateway.springBoot_paymentsApp.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import PaymentGateway.springBoot_paymentsApp.dto.StudentOrder;

public interface StudentOrderRepo extends JpaRepository<StudentOrder,Integer>{

	
public StudentOrder findByRazorpayOrderId(String orderId);
}
