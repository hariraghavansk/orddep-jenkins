package com.sl.ms.ordermanagement;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController 
public class OrderController {
	
	@Autowired
	private OrdersRepo ordersrepo;
	
	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private JwtUtil jwtTokenUtil;
	
	@Autowired
	private MyUserDetailsService userDetailsService;
	
	@GetMapping("/orders")
	public List<Orders> findAllOrders(){
		return ordersrepo.findAll();		
	}
	
	@GetMapping("/orders/{order_id}")
	public ResponseEntity<Orders> findOne(@PathVariable Integer order_id) throws OrderNotfoundException {
		Orders order = ordersrepo.findById(order_id).orElseThrow(() -> new OrderNotfoundException("Invalid order id : " + order_id));
		return ResponseEntity.ok().body(order);
	}
	
	@PostMapping("/orders/{order_id}")
	public String placeOrder(@PathVariable Integer order_id, @RequestBody OrderRequest request)  {
		
		RestTemplate restTemplate = new RestTemplate();
		
		Orders order = request.getOrders();
		List<Items> itemlist = order.getItems();
		
		for(Items item : itemlist) {
			Product product = restTemplate.getForObject("http://localhost:8889/dev/products/"+ item.getItemid(), Product.class);
			 	int quantity = product.getQuantity();
			 	System.out.println(quantity);
				if (quantity == 0)
				{
					return "Product "+ item.getName() +" has quantity as 0 in PRODUCTS";
				}
			}		
		ordersrepo.save(request.getOrders());		
		return "Order placed";
	}
	
	@DeleteMapping("/orders/{order_id}")
	public ResponseEntity<Object> deleteOne(@PathVariable Integer order_id) throws OrderNotfoundException {
		@SuppressWarnings("unused")
		Orders order = ordersrepo.findById(order_id).orElseThrow(() -> new OrderNotfoundException("Invalid order id : " + order_id));
		ordersrepo.deleteById(order_id);
		return new ResponseEntity<>("Order deleted", HttpStatus.OK);
	}
	
	@RequestMapping(value = "/authenticate", method = RequestMethod.POST)
	public ResponseEntity<?> createAuthenticationToken(@RequestBody AuthenticationRequest authenticationRequest) throws Exception {

		try {
			authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(authenticationRequest.getUsername(), authenticationRequest.getPassword())
			);
		}
		catch (BadCredentialsException e) {
			throw new Exception("Incorrect username or password", e);
		}


		final UserDetails userDetails = userDetailsService
				.loadUserByUsername(authenticationRequest.getUsername());

		final String jwt = jwtTokenUtil.generateToken(userDetails);

		return ResponseEntity.ok(new AuthenticationResponse(jwt));
	}
	
}
