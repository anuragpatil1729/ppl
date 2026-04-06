package PaymentGateway.springBoot_paymentsApp.service;

import java.util.Map;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;

import PaymentGateway.springBoot_paymentsApp.dto.StudentOrder;
import PaymentGateway.springBoot_paymentsApp.repo.StudentOrderRepo;

@Service
public class StudentService {

	@Autowired
	private StudentOrderRepo studentRepo;

	@Value("${razorpay.key-id}")
	private String razorpayKey;

	@Value("${razorpay.key-secret}")
	private String razorPaySecret;
	
	private RazorpayClient client;  //this class we got from Razorpay java depandency we have added to communicate with client

	public StudentOrder createOrder(StudentOrder stuOrder) throws Exception {
		
		JSONObject orderReq=new JSONObject ();
		
		orderReq.put("amount", stuOrder.getAmount() * 100); //amount in paisa
		orderReq.put("currency","INR");
		orderReq.put("receipt",stuOrder.getEmail());
		
		this.client = new RazorpayClient(razorpayKey,razorPaySecret);
		
		
		//to create order in razorpay
		
		Order razorPayOrder=client.orders.create(orderReq);
		
		System.out.println(razorPayOrder);
		
		stuOrder.setRazorpayOrderId((String) razorPayOrder.get("id"));
		stuOrder.setOrderStatus((String) razorPayOrder.get("status"));
		
		studentRepo.save(stuOrder);

		return stuOrder;

	}
	
	
	public StudentOrder updateOrder(Map<String,String>responsePayLoad) {
		
		String razorPayOrderId =responsePayLoad.get("razorpay_order_id");
		
		StudentOrder order = studentRepo.findByRazorpayOrderId(razorPayOrderId);
		
		order.setOrderStatus("payment_completed");
		order.setRazorpayPaymentId(responsePayLoad.get("razorpay_payment_id"));
		order.setRazorpaySignature(responsePayLoad.get("razorpay_signature"));
		StudentOrder updatedOrder=studentRepo.save(order);
		
		return updatedOrder;
	}

}
